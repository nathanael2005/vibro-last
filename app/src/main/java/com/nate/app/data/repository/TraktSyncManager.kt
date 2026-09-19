package com.nate.app.data.repository

import android.util.Log
import com.nate.app.data.local.PreferencesManager
import com.nate.app.data.model.Media
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TraktSyncManager(
    private val preferencesManager: PreferencesManager,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        private const val TAG = "TraktSyncManager"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private const val CLIENT_ID = "YOUR_TRAKT_CLIENT_ID_HERE"
        private const val CLIENT_SECRET = "YOUR_TRAKT_CLIENT_SECRET_HERE"
    }

    /**
     * Refreshes the Trakt OAuth2 token if a refresh token exists.
     */
    suspend fun refreshAccessToken(refreshToken: String): String? = withContext(ioDispatcher) {
        try {
            val formBody = FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .add("redirect_uri", "urn:ietf:wg:oauth:2.0:oob")
                .build()

            val request = Request.Builder()
                .url("https://api.trakt.tv/oauth/token")
                .post(formBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)
                val newAccessToken = json.getString("access_token")
                val newRefreshToken = json.getString("refresh_token")
                
                // Save tokens
                // (In production, save to EncryptedSharedPreferences or DataStore)
                Log.d(TAG, "Trakt token refreshed successfully.")
                return@withContext newAccessToken
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh Trakt access token", e)
            null
        }
    }

    /**
     * Sends a scrobbling signal to Trakt to report the current playback progress (0% - 100%).
     */
    suspend fun scrobble(
        accessToken: String,
        mediaType: String,
        tmdbId: Int,
        progress: Double,
        action: String // "start", "pause", "stop"
    ): Boolean = withContext(ioDispatcher) {
        if (accessToken.isBlank()) return@withContext false
        try {
            val url = "https://api.trakt.tv/scrobble/$action"
            val payload = JSONObject().apply {
                val mediaObject = JSONObject().apply {
                    put("ids", JSONObject().apply { put("tmdb", tmdbId) })
                }
                if (mediaType == "tv") {
                    put("episode", mediaObject)
                } else {
                    put("movie", mediaObject)
                }
                put("progress", progress)
            }

            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("trakt-api-version", "2")
                .addHeader("trakt-api-key", CLIENT_ID)
                .build()

            client.newCall(request).execute().use { response ->
                Log.d(TAG, "Scrobble action '$action' returned status code: ${response.code}")
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send Trakt scrobble event", e)
            false
        }
    }

    /**
     * Resolves merge conflicts between local watch history and Trakt cloud records.
     * Synchronizes new watched events dynamically without dropping local data.
     */
    suspend fun synchronizeHistory(accessToken: String): Boolean = withContext(ioDispatcher) {
        if (accessToken.isBlank()) return@withContext false
        try {
            // 1. Fetch watched items from Trakt
            val request = Request.Builder()
                .url("https://api.trakt.tv/sync/watched/movies")
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("trakt-api-version", "2")
                .addHeader("trakt-api-key", CLIENT_ID)
                .build()

            val traktWatchedIds = mutableSetOf<Int>()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val jsonArray = JSONArray(body)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val movie = obj.getJSONObject("movie")
                        val ids = movie.getJSONObject("ids")
                        val tmdbId = ids.optInt("tmdb", -1)
                        if (tmdbId != -1) {
                            traktWatchedIds.add(tmdbId)
                        }
                    }
                }
            }

            // 2. Perform bi-directional local merge
            // Query local watch list from preferences
            // If Trakt has watched it, but we haven't stored it locally, add it.
            // If we have watched it locally but Trakt lacks it, upload it.
            val localList = preferencesManager.watchlistFlow.first()
            val localIds = localList.map { it.id }.toSet()

            val missingLocally = traktWatchedIds.subtract(localIds)
            val missingOnTrakt = localIds.subtract(traktWatchedIds)

            // Sync missing items locally (Mock details as TMDB API would be needed for full metadata)
            for (id in missingLocally) {
                // Add skeleton metadata items to local bookmark database
                val skeletonMedia = Media(
                    id = id,
                    title = "Synced Movie $id",
                    name = null,
                    overview = "Synced from Trakt.tv watch history.",
                    posterPath = null,
                    backdropPath = null,
                    releaseDate = "",
                    firstAirDate = null,
                    voteAverage = 0.0,
                    mediaType = "movie"
                )
                preferencesManager.addToWatchlist(skeletonMedia)
                Log.d(TAG, "Synced watched movie ID $id from Trakt to local watchlist.")
            }

            // Upload missing local watchlist items to Trakt
            if (missingOnTrakt.isNotEmpty()) {
                val uploadPayload = JSONObject().apply {
                    val moviesArray = JSONArray()
                    for (id in missingOnTrakt) {
                        val idsObj = JSONObject()
                        idsObj.put("tmdb", id)
                        val movieObj = JSONObject()
                        movieObj.put("ids", idsObj)
                        moviesArray.put(movieObj)
                    }
                    put("movies", moviesArray)
                }

                val syncReq = Request.Builder()
                    .url("https://api.trakt.tv/sync/history")
                    .post(uploadPayload.toString().toRequestBody(JSON_MEDIA_TYPE))
                    .addHeader("Authorization", "Bearer $accessToken")
                    .addHeader("trakt-api-version", "2")
                    .addHeader("trakt-api-key", CLIENT_ID)
                    .build()

                client.newCall(syncReq).execute().use { res ->
                    Log.d(TAG, "Uploaded missing history to Trakt. Status: ${res.code}")
                }
            }
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Failed Trakt watch history synchronization", e)
            false
        }
    }
}
