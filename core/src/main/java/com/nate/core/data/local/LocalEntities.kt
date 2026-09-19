package com.nate.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nate.core.domain.model.Favorite
import com.nate.core.domain.model.PlaybackResume
import com.nate.core.domain.model.WatchHistory

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val tmdbId: String,
    val title: String,
    val posterPath: String?,
    val mediaType: String,
    val lastWatchedTime: Long,
    val isFinished: Boolean
) {
    fun toDomain(): WatchHistory {
        return WatchHistory(
            tmdbId = tmdbId,
            title = title,
            posterPath = posterPath,
            mediaType = mediaType,
            lastWatchedTime = lastWatchedTime,
            isFinished = isFinished
        )
    }

    companion object {
        fun fromDomain(domain: WatchHistory): WatchHistoryEntity {
            return WatchHistoryEntity(
                tmdbId = domain.tmdbId,
                title = domain.title,
                posterPath = domain.posterPath,
                mediaType = domain.mediaType,
                lastWatchedTime = domain.lastWatchedTime,
                isFinished = domain.isFinished
            )
        }
    }
}

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val tmdbId: String,
    val title: String,
    val posterPath: String?,
    val mediaType: String,
    val addedAt: Long
) {
    fun toDomain(): Favorite {
        return Favorite(
            tmdbId = tmdbId,
            title = title,
            posterPath = posterPath,
            mediaType = mediaType,
            addedAt = addedAt
        )
    }

    companion object {
        fun fromDomain(domain: Favorite): FavoriteEntity {
            return FavoriteEntity(
                tmdbId = domain.tmdbId,
                title = domain.title,
                posterPath = domain.posterPath,
                mediaType = domain.mediaType,
                addedAt = domain.addedAt
            )
        }
    }
}

@Entity(tableName = "playback_resume")
data class PlaybackResumeEntity(
    @PrimaryKey val id: String, // format: "tmdbId_season_episode"
    val tmdbId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long
) {
    fun toDomain(): PlaybackResume {
        return PlaybackResume(
            tmdbId = tmdbId,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            positionMs = positionMs,
            durationMs = durationMs,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun buildId(tmdbId: String, season: Int, episode: Int): String {
            return "${tmdbId}_${season}_${episode}"
        }

        fun fromDomain(domain: PlaybackResume): PlaybackResumeEntity {
            return PlaybackResumeEntity(
                id = buildId(domain.tmdbId, domain.seasonNumber, domain.episodeNumber),
                tmdbId = domain.tmdbId,
                seasonNumber = domain.seasonNumber,
                episodeNumber = domain.episodeNumber,
                positionMs = domain.positionMs,
                durationMs = domain.durationMs,
                updatedAt = domain.updatedAt
            )
        }
    }
}
