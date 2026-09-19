package com.nate.core.data.model

import com.google.gson.annotations.SerializedName
import com.nate.core.domain.model.Category
import com.nate.core.domain.model.MediaItem
import com.nate.core.domain.model.StreamInfo
import com.nate.core.domain.model.StreamLink
import com.nate.core.domain.model.SubtitleLink

data class MediaItemDto(
    @SerializedName("tmdbId") val tmdbId: String,
    @SerializedName("title") val title: String,
    @SerializedName("overview") val overview: String,
    @SerializedName("posterPath") val posterPath: String?,
    @SerializedName("backdropPath") val backdropPath: String?,
    @SerializedName("releaseYear") val releaseYear: String,
    @SerializedName("genres") val genres: List<String>?,
    @SerializedName("rating") val rating: Double?,
    @SerializedName("runtime") val runtime: String?,
    @SerializedName("mediaType") val mediaType: String
) {
    fun toDomain(): MediaItem {
        return MediaItem(
            tmdbId = tmdbId,
            title = title,
            overview = overview,
            posterPath = posterPath,
            backdropPath = backdropPath,
            releaseYear = releaseYear,
            genres = genres ?: emptyList(),
            rating = rating ?: 0.0,
            runtime = runtime,
            mediaType = mediaType
        )
    }
}

data class CategoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("items") val items: List<MediaItemDto>?
) {
    fun toDomain(): Category {
        return Category(
            id = id,
            name = name,
            items = items?.map { it.toDomain() } ?: emptyList()
        )
    }
}

data class StreamInfoDto(
    @SerializedName("tmdbId") val tmdbId: String,
    @SerializedName("title") val title: String,
    @SerializedName("streams") val streams: List<StreamLinkDto>?,
    @SerializedName("subtitles") val subtitles: List<SubtitleLinkDto>?
) {
    fun toDomain(): StreamInfo {
        return StreamInfo(
            tmdbId = tmdbId,
            title = title,
            streams = streams?.map { it.toDomain() } ?: emptyList(),
            subtitles = subtitles?.map { it.toDomain() } ?: emptyList()
        )
    }
}

data class StreamLinkDto(
    @SerializedName("serverId") val serverId: String,
    @SerializedName("serverName") val serverName: String,
    @SerializedName("url") val url: String,
    @SerializedName("format") val format: String,
    @SerializedName("headers") val headers: Map<String, String>?
) {
    fun toDomain(): StreamLink {
        return StreamLink(
            serverId = serverId,
            serverName = serverName,
            url = url,
            format = format,
            headers = headers
        )
    }
}

data class SubtitleLinkDto(
    @SerializedName("lang") val lang: String,
    @SerializedName("label") val label: String,
    @SerializedName("url") val url: String,
    @SerializedName("format") val format: String
) {
    fun toDomain(): SubtitleLink {
        return SubtitleLink(
            lang = lang,
            label = label,
            url = url,
            format = format
        )
    }
}
