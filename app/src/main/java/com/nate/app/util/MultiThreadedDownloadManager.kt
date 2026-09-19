package com.nate.app.util

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

enum class DownloadStatus {
    IDLE,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

class MultiThreadedDownloadManager(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        private const val TAG = "DownloadManager"
        private const val CHUNK_COUNT = 4
    }

    private var status = DownloadStatus.IDLE
    private val bytesDownloaded = AtomicLong(0)
    private var totalBytes = 0L
    private var isPaused = false
    private val writeMutex = Mutex()

    fun getStatus(): DownloadStatus = status
    fun getProgress(): Float = if (totalBytes > 0) bytesDownloaded.get().toFloat() / totalBytes else 0f
    fun pause() {
        isPaused = true
        status = DownloadStatus.PAUSED
    }
    fun resume() {
        isPaused = false
        status = DownloadStatus.DOWNLOADING
    }

    /**
     * Downloads a file using chunked parallel HTTP requests.
     */
    suspend fun download(
        urlStr: String,
        destinationFile: File,
        expectedHash: String? = null,
        onProgressUpdate: (progress: Float, bytes: Long, total: Long) -> Unit
    ): Boolean = withContext(ioDispatcher) {
        status = DownloadStatus.DOWNLOADING
        bytesDownloaded.set(0)
        isPaused = false
        
        try {
            // 1. Resolve content length first
            val contentLength = getContentLength(urlStr)
            if (contentLength <= 0L) {
                throw Exception("Failed to retrieve valid content length from server.")
            }
            totalBytes = contentLength

            // Create placeholder file of exact size
            RandomAccessFile(destinationFile, "rw").use { raf ->
                raf.setLength(contentLength)
            }

            val chunkSize = contentLength / CHUNK_COUNT
            val jobs = mutableListOf<suspend () -> Boolean>()

            for (i in 0 until CHUNK_COUNT) {
                val start = i * chunkSize
                val end = if (i == CHUNK_COUNT - 1) contentLength - 1 else (start + chunkSize - 1)
                
                jobs.add {
                    downloadChunk(urlStr, destinationFile, start, end, onProgressUpdate)
                }
            }

            // Execute all chunks concurrently
            coroutineScope {
                val results = jobs.map { async { it() } }.awaitAll()
                if (results.all { it } && !isPaused) {
                    status = DownloadStatus.COMPLETED
                    Log.d(TAG, "Download completed: ${destinationFile.absolutePath}")
                    
                    // Verify file hash if provided
                    if (expectedHash != null) {
                        val valid = verifyFileHash(destinationFile, expectedHash)
                        if (!valid) {
                            status = DownloadStatus.FAILED
                            throw Exception("SHA-256 Hash Verification Failed!")
                        }
                    }
                    true
                } else {
                    if (isPaused) {
                        status = DownloadStatus.PAUSED
                    } else {
                        status = DownloadStatus.FAILED
                    }
                    false
                }
            }
        } catch (e: Exception) {
            status = DownloadStatus.FAILED
            Log.e(TAG, "Multi-threaded download failed: ${e.message}", e)
            false
        }
    }

    private suspend fun downloadChunk(
        urlStr: String,
        file: File,
        startByte: Long,
        endByte: Long,
        onProgressUpdate: (progress: Float, bytes: Long, total: Long) -> Unit
    ): Boolean {
        var currentStart = startByte
        var retries = 5
        var currentDelay = 1000L

        while (currentStart <= endByte && retries > 0) {
            if (isPaused) {
                delay(500)
                continue
            }

            try {
                val request = Request.Builder()
                    .url(urlStr)
                    .addHeader("Range", "bytes=$currentStart-$endByte")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.code != 206 && response.code != 200) {
                        throw Exception("Server does not support chunked range headers. Code: ${response.code}")
                    }

                    val inputStream = response.body?.byteStream() ?: throw Exception("Response body null")
                    val buffer = ByteArray(8192)
                    var read: Int

                    // Open file in read-write mode to write into the targeted offset
                    RandomAccessFile(file, "rw").use { raf ->
                        raf.seek(currentStart)
                        
                        while (inputStream.read(buffer).also { read = it } != -1) {
                            if (isPaused) {
                                break
                            }
                            
                            raf.write(buffer, 0, read)
                            currentStart += read
                            val totalSoFar = bytesDownloaded.addAndGet(read.toLong())
                            
                            withContext(Dispatchers.Main) {
                                onProgressUpdate(
                                    totalSoFar.toFloat() / totalBytes.toFloat(),
                                    totalSoFar,
                                    totalBytes
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                retries--
                Log.w(TAG, "Chunk error ($startByte-$endByte) failed. Retries remaining: $retries. Err: ${e.message}")
                if (retries > 0) {
                    delay(currentDelay)
                    currentDelay *= 2
                }
            }
        }
        return currentStart > endByte
    }

    private suspend fun getContentLength(urlStr: String): Long = withContext(ioDispatcher) {
        try {
            val req = Request.Builder().url(urlStr).head().build()
            client.newCall(req).execute().use { response ->
                val lenHeader = response.header("Content-Length")
                return@withContext lenHeader?.toLongOrNull() ?: -1L
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch content length", e)
            -1L
        }
    }

    private fun verifyFileHash(file: File, expectedHash: String): Boolean {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            var read: Int
            file.inputStream().use { input ->
                while (input.read(buffer).also { read = it } != -1) {
                    digest.update(buffer, 0, read)
                }
            }
            val hashBytes = digest.digest()
            val sb = StringBuilder()
            for (b in hashBytes) {
                sb.append(String.format("%02x", b))
            }
            val calculatedHash = sb.toString()
            Log.d(TAG, "Calculated file hash: $calculatedHash vs Expected: $expectedHash")
            calculatedHash.equals(expectedHash, ignoreCase = true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed verifying checksum", e)
            false
        }
    }
}
