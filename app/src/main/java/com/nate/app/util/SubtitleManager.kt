package com.nate.app.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MimeTypes
import androidx.media3.common.MediaItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class SubtitleManager(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        private const val TAG = "SubtitleManager"
    }

    /**
     * Downloads an external subtitle (.srt or .vtt), detects and normalizes its encoding to UTF-8,
     * caches it locally, and generates an ExoPlayer-ready SubtitleConfiguration.
     */
    suspend fun downloadAndPrepareSubtitle(
        subtitleUrl: String,
        label: String = "English",
        language: String = "en"
    ): MediaItem.SubtitleConfiguration? = withContext(ioDispatcher) {
        try {
            val request = Request.Builder()
                .url(subtitleUrl)
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Failed to download subtitle. HTTP code: ${response.code}")
                }
                
                val bytes = response.body?.bytes() ?: return@withContext null
                
                // Detect encoding (UTF-8 vs ISO-8859-1)
                val charset = detectCharset(bytes)
                val decodedString = String(bytes, charset)
                
                // Determine mime type and target extension
                val lowercaseUrl = subtitleUrl.lowercase()
                val (mimeType, extension) = when {
                    lowercaseUrl.contains(".vtt") || decodedString.startsWith("WEBVTT") -> {
                        Pair(MimeTypes.TEXT_VTT, "vtt")
                    }
                    else -> {
                        Pair(MimeTypes.APPLICATION_SUBRIP, "srt")
                    }
                }
                
                // Write normalized UTF-8 string to a cache file
                val cacheDir = File(context.cacheDir, "subtitles")
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }
                val localFile = File(cacheDir, "subtitle_${System.currentTimeMillis()}.$extension")
                
                FileOutputStream(localFile).use { fos ->
                    fos.write(decodedString.toByteArray(StandardCharsets.UTF_8))
                }
                
                Log.d(TAG, "Subtitle cached successfully at: ${localFile.absolutePath} (Encoding: ${charset.name()})")
                
                MediaItem.SubtitleConfiguration.Builder(Uri.fromFile(localFile))
                    .setMimeType(mimeType)
                    .setLanguage(language)
                    .setLabel(label)
                    .setRoleFlags(androidx.media3.common.C.ROLE_FLAG_SUBTITLE)
                    .build()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching/parsing subtitle from $subtitleUrl", e)
            null
        }
    }

    /**
     * Basic charset checker based on byte patterns to distinguish between UTF-8 and ISO-8859-1.
     */
    private fun detectCharset(bytes: ByteArray): Charset {
        var i = 0
        val size = bytes.size
        while (i < size) {
            val raw = bytes[i].toInt() and 0xFF
            if (raw < 0x80) {
                i++
                continue
            }
            // Multibyte check for UTF-8 compatibility
            if (raw in 0xC2..0xDF) {
                if (i + 1 < size && (bytes[i + 1].toInt() and 0xC0) == 0x80) {
                    i += 2
                    continue
                }
            } else if (raw in 0xE0..0xEF) {
                if (i + 2 < size &&
                    (bytes[i + 1].toInt() and 0xC0) == 0x80 &&
                    (bytes[i + 2].toInt() and 0xC0) == 0x80
                ) {
                    i += 3
                    continue
                }
            }
            // If pattern matching fails, treat as ISO-8859-1 (Latin1)
            return Charset.forName("ISO-8859-1")
        }
        return StandardCharsets.UTF_8
    }

    /**
     * Clears all cached subtitle files to prevent memory leak / disk filling.
     */
    fun clearCache() {
        try {
            val cacheDir = File(context.cacheDir, "subtitles")
            if (cacheDir.exists() && cacheDir.isDirectory) {
                cacheDir.listFiles()?.forEach { it.delete() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning subtitle cache", e)
        }
    }
}
