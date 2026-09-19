package com.nate.core.domain.repository

import com.nate.core.domain.model.Category
import com.nate.core.domain.model.Favorite
import com.nate.core.domain.model.MediaItem
import com.nate.core.domain.model.PlaybackResume
import com.nate.core.domain.model.StreamInfo
import com.nate.core.domain.model.WatchHistory
import kotlinx.coroutines.flow.Flow

interface MediaRepository {

    // Remote Network APIs
    suspend fun getCategories(): List<Category>

    suspend fun getMoviesCategories(): List<Category>

    suspend fun getTvCategories(): List<Category>

    suspend fun getTrending(page: Int): List<MediaItem>

    suspend fun searchMedia(query: String, page: Int): List<MediaItem>

    suspend fun getMediaDetail(type: String, tmdbId: String): MediaItem

    suspend fun getFullMediaDetail(type: String, tmdbId: String): com.nate.core.domain.model.MediaDetail

    suspend fun getTvSeasonDetail(tvId: String, seasonNumber: Int): com.nate.core.domain.model.SeasonDetail

    suspend fun getStreamInfo(
        tmdbId: String,
        type: String,
        season: Int?,
        episode: Int?
    ): StreamInfo

    // Local Watch History
    fun getWatchHistory(): Flow<List<WatchHistory>>
    suspend fun saveWatchHistory(item: WatchHistory)
    suspend fun deleteWatchHistory(tmdbId: String)
    suspend fun clearWatchHistory()

    // Local Favorites
    fun getFavorites(): Flow<List<Favorite>>
    fun isFavorite(tmdbId: String): Flow<Boolean>
    suspend fun saveFavorite(item: Favorite)
    suspend fun deleteFavorite(tmdbId: String)

    // Local Playback Resume
    suspend fun getPlaybackResume(tmdbId: String, season: Int, episode: Int): PlaybackResume?
    suspend fun savePlaybackResume(item: PlaybackResume)
    suspend fun deletePlaybackResume(tmdbId: String, season: Int, episode: Int)
}
