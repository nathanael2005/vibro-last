package com.nate.core.di

import android.content.Context
import androidx.room.Room
import com.nate.core.data.local.AppDatabase
import com.nate.core.data.local.FavoritesDao
import com.nate.core.data.local.PlaybackResumeDao
import com.nate.core.data.local.WatchHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "onstream.db"
        ).build()
    }

    @Provides
    fun provideWatchHistoryDao(database: AppDatabase): WatchHistoryDao {
        return database.watchHistoryDao()
    }

    @Provides
    fun provideFavoritesDao(database: AppDatabase): FavoritesDao {
        return database.favoritesDao()
    }

    @Provides
    fun providePlaybackResumeDao(database: AppDatabase): PlaybackResumeDao {
        return database.playbackResumeDao()
    }
}
