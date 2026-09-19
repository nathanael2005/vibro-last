package com.nate.tv.presentation.player

data class PlaybackActions(
    val onPlayPause: () -> Unit,
    val onSeekForward: () -> Unit,
    val onSeekBackward: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onOpenServerSelector: () -> Unit,
    val onOpenSubtitles: () -> Unit,
    val onOpenAudioTracks: () -> Unit,
    val onOpenQuality: () -> Unit,
    val onOpenEpisodesSheet: () -> Unit,
    val onNextEpisode: () -> Unit,
    val onToggleAspectRatio: () -> Unit,
    val onToggleSpeed: () -> Unit,
    val onCancelCountdown: () -> Unit,
    val onBack: () -> Unit
)
