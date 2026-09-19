package com.nate.tv.presentation.player

import com.nate.core.domain.model.EpisodeItem
import com.nate.core.domain.model.StreamLink
import com.nate.core.domain.model.SubtitleLink
import androidx.media3.common.TrackGroup

sealed interface PlayerUiState {
    object Idle : PlayerUiState

    data class Sniffing(
        val title: String,
        val subtitleInfo: String?,
        val serverName: String,
        val statusMessage: String,
        val fallbackEmbedUrl: String
    ) : PlayerUiState

    data class Ready(
        val title: String,
        val subtitleInfo: String?,
        val activeStream: StreamLink,
        val availableStreams: List<StreamLink> = emptyList(),
        val initialPositionMs: Long = 0L,
        val durationMs: Long = 0L,
        val isPlaying: Boolean = true,
        val isBuffering: Boolean = false,
        val playbackSpeed: Float = 1.0f,
        val aspectRatioMode: String = "Fit",
        val hasNextEpisode: Boolean = false,
        val nextEpisodeCountdown: Int? = null,
        val fallbackEmbedUrl: String = ""
    ) : PlayerUiState

    data class WebViewFallback(
        val title: String,
        val subtitleInfo: String?,
        val embedUrl: String,
        val serverName: String,
        val availableEmbedServers: List<EmbedServerOption>,
        val reason: String? = null
    ) : PlayerUiState

    data class Error(
        val title: String = "Playback Error",
        val message: String,
        val canRetry: Boolean = true,
        val fallbackEmbedUrl: String? = null
    ) : PlayerUiState
}

data class EmbedServerOption(
    val serverId: String,
    val serverName: String,
    val embedUrl: String
)

data class SubtitleTrack(
    val id: String,
    val label: String,
    val lang: String,
    val isSelected: Boolean,
    val trackGroup: TrackGroup? = null,
    val trackIndex: Int = -1
)

data class AudioTrack(
    val index: Int,
    val label: String,
    val language: String,
    val isSelected: Boolean,
    val trackGroup: TrackGroup? = null,
    val trackIndex: Int = -1
)

