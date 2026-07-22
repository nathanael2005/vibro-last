package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object GitHubUpdateManager {
    private const val TAG = "GitHubUpdateManager"
    private const val LATEST_RELEASE_URL = "https://api.github.com/repos/nathanael2005/vibro-last/releases/latest"
    
    data class UpdateInfo(
        val hasUpdate: Boolean,
        val latestVersion: String,
        val releaseNotes: String,
        val downloadUrl: String,
        val apkSize: Long
    )

    private val client = OkHttpClient.Builder().build()

    suspend fun checkForUpdates(currentVersion: String): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val builder = Request.Builder()
                .url(LATEST_RELEASE_URL)
                .header("User-Agent", "VibroMarketplace-Updater")
                .header("Accept", "application/vnd.github.v3+json")

            val token = BuildConfig.GITHUB_TOKEN
            if (token.isNotEmpty() && token != "YOUR_GITHUB_TOKEN_HERE" && token.startsWith("ghp_")) {
                builder.header("Authorization", "token $token")
            }

            val request = builder.build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Failed to check update: HTTP code ${response.code}")
                    return@withContext null
                }
                val bodyString = response.body?.string() ?: return@withContext null
                val json = JSONObject(bodyString)
                val latestTag = json.optString("tag_name", "")
                val releaseNotes = json.optString("body", "No release notes provided.")
                
                val assets = json.optJSONArray("assets")
                var downloadUrl = ""
                var apkSize = 0L
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk")) {
                            downloadUrl = asset.optString("browser_download_url", "")
                            apkSize = asset.optLong("size", 0L)
                            break
                        }
                    }
                }

                if (latestTag.isNotEmpty() && downloadUrl.isNotEmpty()) {
                    val hasUpdate = isNewerVersion(currentVersion, latestTag)
                    return@withContext UpdateInfo(
                        hasUpdate = hasUpdate,
                        latestVersion = latestTag,
                        releaseNotes = releaseNotes,
                        downloadUrl = downloadUrl,
                        apkSize = apkSize
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking updates from GitHub", e)
        }
        return@withContext null
    }

    private fun isNewerVersion(current: String, latest: String): Boolean {
        val cleanCurrent = current.trim().lowercase().removePrefix("v")
        val cleanLatest = latest.trim().lowercase().removePrefix("v")
        if (cleanCurrent == cleanLatest) return false

        val currentParts = cleanCurrent.split(".").map { it.toIntOrNull() ?: 0 }
        val latestParts = cleanLatest.split(".").map { it.toIntOrNull() ?: 0 }

        val maxLen = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until maxLen) {
            val currVal = currentParts.getOrElse(i) { 0 }
            val latVal = latestParts.getOrElse(i) { 0 }
            if (latVal > currVal) return true
            if (currVal > latVal) return false
        }
        return false
    }

    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Float) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(downloadUrl)
                .header("User-Agent", "VibroMarketplace-Updater")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Failed to download update: HTTP code ${response.code}")
                    return@withContext null
                }
                val body = response.body ?: return@withContext null
                val totalBytes = body.contentLength()
                
                // Store in cache dir defined in file_paths.xml
                val cacheDir = File(context.externalCacheDir ?: context.cacheDir, "updates")
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }
                val apkFile = File(cacheDir, "vibro_update.apk")
                if (apkFile.exists()) {
                    apkFile.delete()
                }

                try {
                    body.byteStream().use { inputStream ->
                        FileOutputStream(apkFile).use { outputStream ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            var totalRead = 0L
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                                totalRead += bytesRead
                                if (totalBytes > 0) {
                                    val progress = totalRead.toFloat() / totalBytes
                                    onProgress(progress)
                                }
                            }
                            outputStream.flush()
                        }
                    }
                    return@withContext apkFile
                } catch (e: Exception) {
                    Log.e(TAG, "Error writing APK file streams", e)
                    if (apkFile.exists()) {
                        apkFile.delete()
                    }
                    return@withContext null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading update APK", e)
        }
        return@withContext null
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    Toast.makeText(context, "Please allow Vibro to install updates from settings.", Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return
                }
            }

            val authority = "${context.packageName}.fileprovider"
            val apkUri = FileProvider.getUriForFile(context, authority, apkFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching APK installation", e)
        }
    }
}
