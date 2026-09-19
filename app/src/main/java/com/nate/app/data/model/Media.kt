package com.nate.app.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class TmdbResponse<T>(
    @SerializedName("results") val results: List<T>,
    @SerializedName("page") val page: Int,
    @SerializedName("total_pages") val totalPages: Int,
)

@Parcelize
data class Media(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("media_type") var mediaType: String? = "movie",
    @SerializedName("runtime") val runtime: Int? = null,
    @SerializedName("genres") val genres: List<Genre>? = null,
    @SerializedName("tagline") val tagline: String? = null,
    @SerializedName("status") val status: String? = null,
) : Parcelable {
    val displayTitle: String
        get() = title ?: name ?: "Unknown Title"

    val displayDate: String
        get() = releaseDate?.takeIf { it.isNotEmpty() }
            ?: firstAirDate?.takeIf { it.isNotEmpty() }
            ?: "N/A"

    val posterUrl: String?
        get() = posterPath?.takeIf { it.isNotEmpty() }?.let { "https://image.tmdb.org/t/p/w500$it" }

    val backdropUrl: String?
        get() = backdropPath?.takeIf { it.isNotEmpty() }?.let { "https://image.tmdb.org/t/p/w1280$it" }

    val runtimeText: String
        get() = runtime?.let {
            val hours = it / 60
            val minutes = it % 60
            if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        } ?: ""

    val genreText: String
        get() = genres?.joinToString(", ") { it.name } ?: ""
}

@Parcelize
data class Genre(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
) : Parcelable

data class TvShowDetails(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("overview") val overview: String?,
    @SerializedName("seasons") val seasons: List<Season>?,
    @SerializedName("genres") val genres: List<Genre>? = null,
    @SerializedName("tagline") val tagline: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("number_of_episodes") val numberOfEpisodes: Int? = null,
    @SerializedName("number_of_seasons") val numberOfSeasons: Int? = null,
    @SerializedName("episode_run_time") val episodeRunTime: List<Int>? = null,
    @SerializedName("first_air_date") val firstAirDate: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("vote_average") val voteAverage: Double? = null,
) {
    val genreText: String
        get() = genres?.joinToString(", ") { it.name } ?: ""

    val runtimeText: String
        get() = episodeRunTime?.firstOrNull()?.let {
            "${it}m"
        } ?: ""
}

data class Season(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("season_number") val seasonNumber: Int,
    @SerializedName("episode_count") val episodeCount: Int,
    @SerializedName("poster_path") val posterPath: String?,
)

data class SeasonDetails(
    @SerializedName("id") val id: Int,
    @SerializedName("season_number") val seasonNumber: Int,
    @SerializedName("episodes") val episodes: List<Episode>,
)

data class Episode(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("overview") val overview: String?,
    @SerializedName("episode_number") val episodeNumber: Int,
    @SerializedName("season_number") val seasonNumber: Int,
    @SerializedName("still_path") val stillPath: String?,
) {
    val stillUrl: String?
        get() = stillPath?.takeIf { it.isNotEmpty() }?.let { "https://image.tmdb.org/t/p/w300$it" }
}
