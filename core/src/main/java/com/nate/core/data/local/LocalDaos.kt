package com.nate.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {

    @Query("SELECT * FROM watch_history ORDER BY lastWatchedTime DESC")
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchHistory(item: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE tmdbId = :tmdbId")
    suspend fun deleteWatchHistory(tmdbId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearWatchHistory()
}

@Dao
interface FavoritesDao {

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE tmdbId = :tmdbId)")
    fun isFavorite(tmdbId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(item: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE tmdbId = :tmdbId")
    suspend fun deleteFavorite(tmdbId: String)
}

@Dao
interface PlaybackResumeDao {

    @Query("SELECT * FROM playback_resume WHERE id = :id LIMIT 1")
    suspend fun getPlaybackResumeById(id: String): PlaybackResumeEntity?

    @Query("SELECT * FROM playback_resume WHERE tmdbId = :tmdbId")
    suspend fun getResumePositionsForMedia(tmdbId: String): List<PlaybackResumeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaybackResume(item: PlaybackResumeEntity)

    @Query("DELETE FROM playback_resume WHERE id = :id")
    suspend fun deletePlaybackResumeById(id: String)
}
