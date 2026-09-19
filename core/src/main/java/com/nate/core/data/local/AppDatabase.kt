package com.nate.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        WatchHistoryEntity::class,
        FavoriteEntity::class,
        PlaybackResumeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun favoritesDao(): FavoritesDao
    abstract fun playbackResumeDao(): PlaybackResumeDao
}
