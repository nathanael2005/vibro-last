package com.nate.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nate.app.BuildConfig
import com.nate.app.data.model.Media
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nate_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        const val DEFAULT_TMDB_KEY = "21a41952baa4e07ec88eecc83aa06014"
        val TMDB_API_KEY = stringPreferencesKey("tmdb_api_key")
        val WATCHLIST_ITEMS = stringSetPreferencesKey("watchlist_items")
        val PLAYBACK_POSITIONS = stringSetPreferencesKey("playback_positions")
    }

    val apiKeyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        val key = preferences[TMDB_API_KEY]?.trim().orEmpty()
        if (key.isNotEmpty()) {
            key
        } else {
            val configKey = BuildConfig.TMDB_API_KEY.trim()
            val decrypted = if (configKey.isNotEmpty()) {
                com.nate.app.util.SecurityUtils.decrypt(configKey, "NateStream").trim()
            } else {
                ""
            }
            if (decrypted.isNotEmpty()) decrypted else DEFAULT_TMDB_KEY
        }
    }

    suspend fun setApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[TMDB_API_KEY] = apiKey.trim()
        }
    }

    // id|type|title|poster|overview|backdrop|vote|releaseDate|runtime|genres|tagline|status
    val watchlistFlow: Flow<List<Media>> = context.dataStore.data.map { preferences ->
        val set = preferences[WATCHLIST_ITEMS] ?: emptySet()
        set.mapNotNull { serialized -> deserializeMedia(serialized) }
    }

    suspend fun addToWatchlist(media: Media) {
        context.dataStore.edit { preferences ->
            val currentSet = preferences[WATCHLIST_ITEMS]?.toMutableSet() ?: mutableSetOf()
            currentSet.removeAll { it.startsWith("${media.id}|${media.mediaType ?: "movie"}|") }
            currentSet.add(serializeMedia(media))
            preferences[WATCHLIST_ITEMS] = currentSet
        }
    }

    suspend fun removeFromWatchlist(mediaId: Int, mediaType: String? = null) {
        context.dataStore.edit { preferences ->
            val currentSet = preferences[WATCHLIST_ITEMS]?.toMutableSet() ?: mutableSetOf()
            val target = currentSet.find { entry ->
                entry.startsWith("$mediaId|") &&
                    (mediaType == null || entry.startsWith("$mediaId|$mediaType|"))
            }
            if (target != null) {
                currentSet.remove(target)
                preferences[WATCHLIST_ITEMS] = currentSet
            }
        }
    }

    fun isBookmarkedFlow(mediaId: Int, mediaType: String): Flow<Boolean> = context.dataStore.data.map { preferences ->
        val set = preferences[WATCHLIST_ITEMS] ?: emptySet()
        set.any { it.startsWith("$mediaId|$mediaType|") }
    }

    fun resumePositionMsFlow(
        mediaId: Int,
        mediaType: String,
        season: Int = 1,
        episode: Int = 1,
    ): Flow<Long> = context.dataStore.data.map { preferences ->
        val key = playbackKey(mediaId, mediaType, season, episode)
        val raw = preferences[PLAYBACK_POSITIONS]?.find { it.startsWith("$key|") }
        raw?.substringAfterLast('|')?.toLongOrNull() ?: 0L
    }

    suspend fun getResumePositionMs(
        mediaId: Int,
        mediaType: String,
        season: Int = 1,
        episode: Int = 1,
    ): Long = resumePositionMsFlow(mediaId, mediaType, season, episode).first()

    suspend fun saveResumePosition(
        mediaId: Int,
        mediaType: String,
        season: Int,
        episode: Int,
        positionMs: Long,
    ) {
        if (positionMs < 5_000L) return
        val key = playbackKey(mediaId, mediaType, season, episode)
        context.dataStore.edit { preferences ->
            val current = preferences[PLAYBACK_POSITIONS]?.toMutableSet() ?: mutableSetOf()
            current.removeAll { it.startsWith("$key|") }
            current.add("$key|$positionMs")
            preferences[PLAYBACK_POSITIONS] = current
        }
    }

    suspend fun clearResumePosition(
        mediaId: Int,
        mediaType: String,
        season: Int,
        episode: Int,
    ) {
        val key = playbackKey(mediaId, mediaType, season, episode)
        context.dataStore.edit { preferences ->
            val current = preferences[PLAYBACK_POSITIONS]?.toMutableSet() ?: mutableSetOf()
            current.removeAll { it.startsWith("$key|") }
            preferences[PLAYBACK_POSITIONS] = current
        }
    }

    private fun playbackKey(mediaId: Int, mediaType: String, season: Int, episode: Int): String =
        if (mediaType == "tv") "$mediaId|tv|$season|$episode" else "$mediaId|movie"

    private fun serializeMedia(media: Media): String {
        val type = media.mediaType ?: "movie"
        val genresJson = media.genres?.joinToString(";") { "${it.id},${it.name}" } ?: ""
        return listOf(
            media.id.toString(),
            type,
            media.displayTitle.replace("|", "/"),
            media.posterPath ?: "null",
            (media.overview ?: "").replace("|", "/").take(500),
            media.backdropPath ?: "null",
            (media.voteAverage ?: 0.0).toString(),
            media.displayDate.replace("|", "/"),
            (media.runtime ?: 0).toString(),
            genresJson,
            (media.tagline ?: "").replace("|", "/"),
            (media.status ?: "").replace("|", "/"),
        ).joinToString("|")
    }

    private fun deserializeMedia(serialized: String): Media? {
        return try {
            val parts = serialized.split("|")
            if (parts.size < 4) return null
            val id = parts[0].toInt()
            val type = parts[1]
            val title = parts[2]
            val posterPath = parts[3].takeIf { it != "null" }
            val overview = parts.getOrNull(4)?.takeIf { it.isNotEmpty() } ?: ""
            val backdropPath = parts.getOrNull(5)?.takeIf { it != "null" } ?: ""
            val vote = parts.getOrNull(6)?.toDoubleOrNull() ?: 0.0
            val date = parts.getOrNull(7) ?: ""
            val runtime = parts.getOrNull(8)?.toIntOrNull()
            val genresJson = parts.getOrNull(9) ?: ""
            val genres = genresJson.split(";").mapNotNull {
                val gParts = it.split(",")
                if (gParts.size == 2) com.nate.app.data.model.Genre(gParts[0].toInt(), gParts[1]) else null
            }.takeIf { it.isNotEmpty() }
            val tagline = parts.getOrNull(10)
            val status = parts.getOrNull(11)

            Media(
                id = id,
                title = if (type == "movie") title else null,
                name = if (type == "tv") title else null,
                overview = overview,
                posterPath = posterPath,
                backdropPath = backdropPath,
                releaseDate = if (type == "movie") date else "",
                firstAirDate = if (type == "tv") date else "",
                voteAverage = vote,
                mediaType = type,
                runtime = runtime,
                genres = genres,
                tagline = tagline,
                status = status,
            )
        } catch (_: Exception) {
            null
        }
    }
}
