package com.nate.app.data.repository

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

enum class DebridProvider {
    REAL_DEBRID,
    PREMIUMIZE,
    ALL_DEBRID
}

class DebridService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        private const val TAG = "DebridService"
    }

    /**
     * Checks if a magnet link's torrent is already cached on the Debrid server.
     * Caching allows instant streaming of torrents without waiting for download cycles.
     */
    suspend fun checkInstantAvailability(
        provider: DebridProvider,
        apiKey: String,
        infoHash: String
    ): Boolean = withContext(ioDispatcher) {
        if (apiKey.isBlank()) return@withContext false
        try {
            when (provider) {
                DebridProvider.REAL_DEBRID -> {
                    val url = "https://api.real-debrid.com/rest/1.0/torrents/instantAvailability/$infoHash"
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer $apiKey")
                        .build()
                    
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return@withContext false
                        val body = response.body?.string().orEmpty()
                        val json = JSONObject(body)
                        // Checks if the json has files under the requested hash
                        if (json.has(infoHash)) {
                            val hashObj = json.getJSONObject(infoHash)
                            if (hashObj.has("rd")) {
                                val rdArr = hashObj.getJSONArray("rd")
                                return@withContext rdArr.length() > 0
                            }
                        }
                    }
                }
                DebridProvider.PREMIUMIZE -> {
                    val url = "https://www.premiumize.me/api/cache/check?items[]=$infoHash"
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer $apiKey")
                        .build()
                    
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return@withContext false
                        val body = response.body?.string().orEmpty()
                        val json = JSONObject(body)
                        if (json.optString("status") == "success") {
                            val responseArr = json.getJSONArray("response")
                            if (responseArr.length() > 0) {
                                return@withContext responseArr.getBoolean(0)
                            }
                        }
                    }
                }
                DebridProvider.ALL_DEBRID -> {
                    val url = "https://api.alldebrid.com/v4/magnet/instant?agent=NateApp&apikey=$apiKey&magnets[]=$infoHash"
                    val request = Request.Builder().url(url).build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return@withContext false
                        val body = response.body?.string().orEmpty()
                        val json = JSONObject(body)
                        if (json.optString("status") == "success") {
                            val data = json.getJSONObject("data")
                            val magnets = data.getJSONArray("magnets")
                            if (magnets.length() > 0) {
                                return@withContext magnets.getJSONObject(0).optBoolean("instant", false)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking instant cache availability on ${provider.name}", e)
        }
        return@withContext false
    }

    /**
     * Unrestricts a file hosting link or generates a streaming URL from a cached torrent link.
     */
    suspend fun unrestrictLink(
        provider: DebridProvider,
        apiKey: String,
        linkUrl: String
    ): String? = withContext(ioDispatcher) {
        if (apiKey.isBlank() || linkUrl.isBlank()) return@withContext null
        try {
            when (provider) {
                DebridProvider.REAL_DEBRID -> {
                    val url = "https://api.real-debrid.com/rest/1.0/unrestrict/link"
                    val formBody = FormBody.Builder()
                        .add("link", linkUrl)
                        .build()
                    
                    val request = Request.Builder()
                        .url(url)
                        .post(formBody)
                        .addHeader("Authorization", "Bearer $apiKey")
                        .build()
                    
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return@withContext null
                        val body = response.body?.string().orEmpty()
                        val json = JSONObject(body)
                        return@withContext json.optString("download").takeIf { it.isNotEmpty() }
                    }
                }
                DebridProvider.PREMIUMIZE -> {
                    // Premiumize handles links directly through folder listings or transfer requests
                    val url = "https://www.premiumize.me/api/transfer/create"
                    val formBody = FormBody.Builder()
                        .add("src", linkUrl)
                        .build()
                    
                    val request = Request.Builder()
                        .url(url)
                        .post(formBody)
                        .addHeader("Authorization", "Bearer $apiKey")
                        .build()
                    
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return@withContext null
                        val body = response.body?.string().orEmpty()
                        val json = JSONObject(body)
                        if (json.optString("status") == "success") {
                            // Returns the location if it is instant cached
                            return@withContext json.optString("location").takeIf { it.isNotEmpty() }
                        }
                    }
                }
                DebridProvider.ALL_DEBRID -> {
                    val url = "https://api.alldebrid.com/v4/link/unlock?agent=NateApp&apikey=$apiKey&link=$linkUrl"
                    val request = Request.Builder().url(url).build()
                    
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return@withContext null
                        val body = response.body?.string().orEmpty()
                        val json = JSONObject(body)
                        if (json.optString("status") == "success") {
                            val data = json.getJSONObject("data")
                            return@withContext data.optString("link").takeIf { it.isNotEmpty() }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unrestrict link via ${provider.name}", e)
        }
        return@withContext null
    }
}
