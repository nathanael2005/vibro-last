package com.nate.core.domain.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class MediaItem(
    val tmdbId: String,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseYear: String,
    val genres: List<String>,
    val rating: Double,
    val runtime: String?,
    val mediaType: String // "movie" or "tv"
) : Parcelable

@Immutable
@Parcelize
data class EpisodeItem(
    val id: Int,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val name: String,
    val overview: String,
    val stillPath: String?,
    val runtime: String?,
    val voteAverage: Double?
) : Parcelable

@Immutable
@Parcelize
data class SeasonInfo(
    val seasonNumber: Int,
    val name: String,
    val episodeCount: Int,
    val posterPath: String?
) : Parcelable

@Immutable
@Parcelize
data class SeasonDetail(
    val seasonNumber: Int,
    val name: String,
    val episodes: List<EpisodeItem>
) : Parcelable

@Immutable
@Parcelize
data class MediaDetail(
    val mediaItem: MediaItem,
    val numberOfSeasons: Int,
    val numberOfEpisodes: Int,
    val genres: List<String>,
    val runtime: String?,
    val tagline: String?,
    val seasons: List<SeasonInfo> = emptyList(),
    val similar: List<MediaItem> = emptyList()
) : Parcelable

@Immutable
data class Category(
    val id: String,
    val name: String,
    val items: List<MediaItem>
)

@Immutable
data class StreamInfo(
    val tmdbId: String,
    val title: String,
    val streams: List<StreamLink>,
    val subtitles: List<SubtitleLink>
)

@Immutable
data class StreamLink(
    val serverId: String,
    val serverName: String,
    val url: String,
    val format: String, // "HLS", "DASH", "MP4"
    val headers: Map<String, String>? = null,
    val subtitles: List<SubtitleLink> = emptyList()
)

@Immutable
data class SubtitleLink(
    val lang: String,
    val label: String,
    val url: String,
    val format: String // "VTT", "SRT"
)

@Immutable
data class WatchHistory(
    val tmdbId: String,
    val title: String,
    val posterPath: String?,
    val mediaType: String,
    val lastWatchedTime: Long,
    val isFinished: Boolean
)

@Immutable
data class Favorite(
    val tmdbId: String,
    val title: String,
    val posterPath: String?,
    val mediaType: String,
    val addedAt: Long
)

@Immutable
data class PlaybackResume(
    val tmdbId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long
)
