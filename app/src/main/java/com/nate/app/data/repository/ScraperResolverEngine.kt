package com.nate.app.data.repository

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

data class ScrapedLink(
    val providerName: String,
    val url: String,
    val isVerified: Boolean,
    val quality: String = "Auto",
    val headers: Map<String, String> = emptyMap()
)

data class ProviderConfig(
    val name: String,
    val baseUrl: String,
    val apiEndpoint: String,
    val isEnabled: Boolean
)

class ScraperResolverEngine(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        private const val TAG = "ScraperResolverEngine"
        private const val ALGORITHM = "AES/CBC/PKCS5Padding"
        
        // Static fallbacks for provider base configurations
        private val FALLBACK_PROVIDERS = listOf(
            ProviderConfig("Videasy", "https://player.videasy.net", "/api/source/", true),
            ProviderConfig("VidSrcTo", "https://vidsrc.to", "/embed/", true),
            ProviderConfig("VidSrcMe", "https://vidsrc.me", "/embed/", true),
            ProviderConfig("EmbedSu", "https://embed.su", "/api/e/", true)
        )
    }

    private var activeProviders: List<ProviderConfig> = FALLBACK_PROVIDERS

    /**
     * Decrypts a remote providers payload.
     * The dynamic provider architecture enables updating scraping rules, APIs, and selectors
     * remotely without requiring a rebuild of the application.
     */
    fun updateProvidersFromEncryptedPayload(encryptedPayload: String, aesKey: String, aesIv: String): Boolean {
        return try {
            val keySpec = SecretKeySpec(aesKey.toByteArray(StandardCharsets.UTF_8), "AES")
            val ivSpec = IvParameterSpec(aesIv.toByteArray(StandardCharsets.UTF_8))
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            
            val decodedBytes = Base64.decode(encryptedPayload, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            val jsonStr = String(decryptedBytes, StandardCharsets.UTF_8)
            
            parseProvidersJson(jsonStr)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt and load remote providers config", e)
            false
        }
    }

    private fun parseProvidersJson(jsonStr: String) {
        try {
            val jsonArray = JSONObject(jsonStr).getJSONArray("providers")
            val newList = mutableListOf<ProviderConfig>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                newList.add(
                    ProviderConfig(
                        name = obj.getString("name"),
                        baseUrl = obj.getString("baseUrl"),
                        apiEndpoint = obj.getString("apiEndpoint"),
                        isEnabled = obj.getBoolean("isEnabled")
                    )
                )
            }
            if (newList.isNotEmpty()) {
                activeProviders = newList
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing provider configurations from JSON", e)
        }
    }

    /**
     * Resolves streaming links concurrently across all active providers.
     */
    suspend fun resolveLinks(
        mediaType: String,
        mediaId: Int,
        season: Int = 1,
        episode: Int = 1
    ): List<ScrapedLink> = withContext(ioDispatcher) {
        val jobs = activeProviders.filter { it.isEnabled }.map { provider ->
            async {
                scrapeFromProvider(provider, mediaType, mediaId, season, episode)
            }
        }
        
        jobs.awaitAll()
            .flatten()
            .filter { it.isVerified }
    }

    private suspend fun scrapeFromProvider(
        provider: ProviderConfig,
        mediaType: String,
        mediaId: Int,
        season: Int,
        episode: Int
    ): List<ScrapedLink> {
        val targetUrl = if (mediaType == "tv") {
            "${provider.baseUrl}${provider.apiEndpoint}tv/$mediaId/$season/$episode"
        } else {
            "${provider.baseUrl}${provider.apiEndpoint}movie/$mediaId"
        }

        // Retry with exponential backoff
        var currentDelay = 1000L
        val maxDelay = 8000L
        val factor = 2.0
        val maxRetries = 3

        for (attempt in 1..maxRetries) {
            try {
                Log.d(TAG, "Attempt $attempt: Scraping $targetUrl")
                val requestHeaders = mapOf(
                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                    "Referer" to provider.baseUrl,
                    "Origin" to provider.baseUrl,
                    "Accept" to "application/json, text/javascript, */*; q=0.01"
                )
                
                val reqBuilder = Request.Builder().url(targetUrl)
                requestHeaders.forEach { (k, v) -> reqBuilder.addHeader(k, v) }
                
                client.newCall(reqBuilder.build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("Scraping response code: ${response.code}")
                    }
                    
                    val body = response.body?.string().orEmpty()
                    val links = extractUrlsFromBody(provider.name, body, provider.baseUrl, requestHeaders)
                    
                    // Concurrent pre-flight validation on retrieved links
                    val verifiedLinks = links.map { link ->
                        withContext(ioDispatcher) {
                            val verified = preflightCheck(link.url, requestHeaders)
                            link.copy(isVerified = verified)
                        }
                    }
                    return verifiedLinks
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error scraping provider ${provider.name} (Attempt $attempt): ${e.message}")
                if (attempt < maxRetries) {
                    delay(currentDelay)
                    currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
                }
            }
        }
        return emptyList()
    }

    private fun extractUrlsFromBody(
        providerName: String,
        body: String,
        referer: String,
        headers: Map<String, String>
    ): List<ScrapedLink> {
        val result = mutableListOf<ScrapedLink>()
        try {
            if (body.trim().startsWith("{")) {
                val json = JSONObject(body)
                if (json.has("url")) {
                    result.add(ScrapedLink(providerName, json.getString("url"), false, "Auto", headers))
                } else if (json.has("sources")) {
                    val arr = json.getJSONArray("sources")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        result.add(
                            ScrapedLink(
                                providerName = providerName,
                                url = obj.getString("file"),
                                isVerified = false,
                                quality = obj.optString("label", "Auto"),
                                headers = headers
                            )
                        )
                    }
                }
            } else {
                // Regex extractor for m3u8 or mp4 links embedded inside HTML source code
                val regex = Regex(""""(https?://[^"\s]+\.(?:m3u8|mp4|mpd)(?:\?[^"\s]*)?)"""")
                regex.findAll(body).forEach { match ->
                    val matchedUrl = match.groupValues[1]
                    result.add(ScrapedLink(providerName, matchedUrl, false, "Auto", headers))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting stream links from body", e)
        }
        return result
    }

    /**
     * Performs a pre-flight HEAD check to confirm link validity.
     * Prevents player injection of dead or 403 Forbidden URLs.
     */
    private fun preflightCheck(urlStr: String, customHeaders: Map<String, String>): Boolean {
        return try {
            val requestBuilder = Request.Builder()
                .url(urlStr)
                .head()
            
            customHeaders.forEach { (k, v) -> requestBuilder.addHeader(k, v) }
            
            client.newCall(requestBuilder.build()).execute().use { response ->
                val code = response.code
                Log.d(TAG, "Preflight check for $urlStr returned HTTP $code")
                code in 200..399
            }
        } catch (e: Exception) {
            Log.w(TAG, "Preflight liveness verification failed for $urlStr: ${e.message}")
            false
        }
    }
}
