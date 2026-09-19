package com.nate.tv.presentation.player

import android.content.Context
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nate.core.common.AppLogger
import com.nate.core.data.player.StreamFallbackManager
import com.nate.core.domain.model.EpisodeItem
import com.nate.core.domain.model.PlaybackResume
import com.nate.core.domain.model.StreamLink
import com.nate.core.domain.repository.MediaRepository
import com.nate.tv.presentation.player.extractor.WebViewStreamExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Idle)
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _activeStream = MutableStateFlow<StreamLink?>(null)
    val activeStream: StateFlow<StreamLink?> = _activeStream.asStateFlow()

    private val _availableStreams = MutableStateFlow<List<StreamLink>>(emptyList())
    val availableStreams: StateFlow<List<StreamLink>> = _availableStreams.asStateFlow()

    private val _subtitles = MutableStateFlow<List<SubtitleTrack>>(emptyList())
    val subtitles: StateFlow<List<SubtitleTrack>> = _subtitles.asStateFlow()

    private val _audioTracks = MutableStateFlow<List<AudioTrack>>(emptyList())
    val audioTracks: StateFlow<List<AudioTrack>> = _audioTracks.asStateFlow()

    private val _availableEpisodes = MutableStateFlow<List<EpisodeItem>>(emptyList())
    val availableEpisodes: StateFlow<List<EpisodeItem>> = _availableEpisodes.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _aspectRatioMode = MutableStateFlow("Fit")
    val aspectRatioMode: StateFlow<String> = _aspectRatioMode.asStateFlow()

    private val _subtitleDelayMs = MutableStateFlow(0L)
    val subtitleDelayMs: StateFlow<Long> = _subtitleDelayMs.asStateFlow()

    private val _extractorWebView = MutableStateFlow<WebView?>(null)
    val extractorWebView: StateFlow<WebView?> = _extractorWebView.asStateFlow()

    private var fallbackManager: StreamFallbackManager? = null
    private var streamExtractor: WebViewStreamExtractor? = null
    private var pendingResumePosition: Long = 0L
    private var loadMediaJob: kotlinx.coroutines.Job? = null

    var tmdbId: String = ""
    var mediaType: String = ""
    var season: Int? = null
    var episode: Int? = null
    var mediaTitle: String = ""
    var subtitleInfo: String? = null
    var posterUrl: String? = null
    var availableEmbedServers: List<EmbedServerOption> = emptyList()

    fun loadMedia(
        tmdbId: String,
        type: String,
        season: Int?,
        episode: Int?,
        context: Context,
        resumePositionMs: Long = 0L
    ) {
        // Cancel any pending extraction jobs and release active scraper
        loadMediaJob?.cancel()
        streamExtractor?.release()
        streamExtractor = null
        _extractorWebView.value = null
        _activeStream.value = null
        _availableStreams.value = emptyList()

        this.tmdbId = tmdbId
        this.mediaType = type
        this.season = season
        this.episode = episode
        this.pendingResumePosition = resumePositionMs

        val sNum = season ?: 1
        val epNum = episode ?: 1
        val isTv = type.equals("tv", ignoreCase = true) ||
                type.contains("tv", ignoreCase = true) ||
                type.contains("show", ignoreCase = true) ||
                season != null

        val sInfo = if (isTv) "Season $sNum • Episode $epNum" else "Movie"
        this.subtitleInfo = sInfo

        // Candidate servers: VidSrc Cinema & VidSrc Pro are fastest & verified working
        availableEmbedServers = listOf(
            EmbedServerOption(
                serverId = "vidsrc_to",
                serverName = "🎬 VidSrc Cinema",
                embedUrl = if (isTv) "https://vidsrc.to/embed/tv/$tmdbId/$sNum/$epNum" else "https://vidsrc.to/embed/movie/$tmdbId"
            ),
            EmbedServerOption(
                serverId = "vidsrc_in",
                serverName = "🎬 VidSrc Pro",
                embedUrl = if (isTv) "https://vidsrc.in/embed/tv/$tmdbId/$sNum/$epNum" else "https://vidsrc.in/embed/movie/$tmdbId"
            ),
            EmbedServerOption(
                serverId = "vidsrc_pm",
                serverName = "🎬 VidSrc Prime",
                embedUrl = if (isTv) "https://vidsrc.pm/embed/tv/$tmdbId/$sNum/$epNum" else "https://vidsrc.pm/embed/movie/$tmdbId"
            ),
            EmbedServerOption(
                serverId = "videasy",
                serverName = "⚡ VidEasy Ultra (Fast)",
                embedUrl = if (isTv) "https://player.videasy.net/tv/$tmdbId/$sNum/$epNum?overlay=true" else "https://player.videasy.net/movie/$tmdbId?overlay=true"
            ),
            EmbedServerOption(
                serverId = "vidlink_pro",
                serverName = "⚡ VidLink 1080p Ultra",
                embedUrl = if (isTv) "https://vidlink.pro/tv/$tmdbId/$sNum/$epNum" else "https://vidlink.pro/movie/$tmdbId"
            ),
            EmbedServerOption(
                serverId = "2embed_cc",
                serverName = "📺 2Embed HD",
                embedUrl = if (isTv) "https://www.2embed.cc/embedtv/$tmdbId?s=$sNum&e=$epNum" else "https://www.2embed.cc/embed/$tmdbId"
            ),
            EmbedServerOption(
                serverId = "autoembed_to",
                serverName = "💎 AutoEmbed Mirror",
                embedUrl = if (isTv) "https://autoembed.to/tv/tmdb/$tmdbId/$sNum/$epNum" else "https://autoembed.to/movie/tmdb/$tmdbId"
            )
        )

        val defaultEmbedUrl = availableEmbedServers.first().embedUrl
        AppLogger.player("loadMedia: tmdbId=$tmdbId, type=$type, season=$season, episode=$episode, resumeMs=$resumePositionMs")

        // Synchronously enter Sniffing immediately so Compose unmounts any previous ExoPlayer
        _uiState.value = PlayerUiState.Sniffing(
            title = if (mediaTitle.isNotBlank()) mediaTitle else "Loading...",
            subtitleInfo = sInfo,
            serverName = availableEmbedServers.first().serverName,
            statusMessage = "Resolving high-speed stream...",
            fallbackEmbedUrl = defaultEmbedUrl
        )

        loadMediaJob = viewModelScope.launch {
            try {
                if (isTv) {
                    try {
                        val seasonDetail = repository.getTvSeasonDetail(tmdbId, sNum)
                        _availableEpisodes.value = seasonDetail.episodes
                        val currentEp = seasonDetail.episodes.find { it.episodeNumber == epNum }
                        if (currentEp != null) {
                            subtitleInfo = "S${sNum}:E${epNum} - ${currentEp.name}"
                        }
                    } catch (e: Exception) {
                        AppLogger.error("Failed to load TV season details", e)
                    }
                } else {
                    subtitleInfo = "Movie"
                }

                val mediaDetail = runCatching { repository.getMediaDetail(type, tmdbId) }.getOrNull()
                mediaTitle = mediaDetail?.title ?: if (isTv) "Series" else "Movie"
                posterUrl = mediaDetail?.posterPath

                val savedResumeMs = if (pendingResumePosition > 0L) {
                    pendingResumePosition
                } else {
                    runCatching { repository.getPlaybackResume(tmdbId, season ?: 0, episode ?: 0)?.positionMs ?: 0L }.getOrDefault(0L)
                }

                // Show sleek native sniffing UI with loaded title & episode name
                _uiState.value = PlayerUiState.Sniffing(
                    title = mediaTitle,
                    subtitleInfo = subtitleInfo,
                    serverName = availableEmbedServers.first().serverName,
                    statusMessage = "Resolving high-speed stream & bypassing bot checks...",
                    fallbackEmbedUrl = defaultEmbedUrl
                )

                // Start Headless Background Stream & Subtitle Extractor
                streamExtractor?.release()
                _extractorWebView.value = null
                streamExtractor = WebViewStreamExtractor(
                    context = context,
                    onWebViewCreated = { wv ->
                        _extractorWebView.value = wv
                    },
                    onStreamExtracted = { streamLink ->
                        _extractorWebView.value = null
                        val streams = listOf(streamLink)
                        _availableStreams.value = streams
                        _activeStream.value = streamLink
                        fallbackManager = StreamFallbackManager(streams)

                        val episodes = _availableEpisodes.value
                        val hasNext = if (isTv && episode != null) {
                            episodes.any { it.episodeNumber > (episode ?: 0) }
                        } else false

                        AppLogger.player("🎬 Transitioning to Native ExoPlayer Ready state: ${streamLink.url} (resuming at ${savedResumeMs}ms)")

                        _uiState.value = PlayerUiState.Ready(
                            title = mediaTitle,
                            subtitleInfo = subtitleInfo,
                            activeStream = streamLink,
                            availableStreams = streams,
                            initialPositionMs = savedResumeMs,
                            durationMs = 0L,
                            isPlaying = true,
                            isBuffering = true,
                            playbackSpeed = _playbackSpeed.value,
                            aspectRatioMode = _aspectRatioMode.value,
                            hasNextEpisode = hasNext,
                            fallbackEmbedUrl = defaultEmbedUrl
                        )
                    },
                    onAllExtractionsFailed = {
                        _extractorWebView.value = null
                        AppLogger.error("All extraction candidates failed.")
                        _uiState.value = PlayerUiState.Error(
                            title = mediaTitle,
                            message = "Could not resolve a playable stream from available providers. Please try again.",
                            canRetry = true,
                            fallbackEmbedUrl = defaultEmbedUrl
                        )
                    },
                    onStatusUpdate = { sName, statusText ->
                        val current = _uiState.value
                        if (current is PlayerUiState.Sniffing) {
                            _uiState.value = current.copy(
                                serverName = sName,
                                statusMessage = statusText
                            )
                        }
                    }
                )

                streamExtractor?.startExtraction(tmdbId, type, season, episode)

            } catch (e: Exception) {
                AppLogger.error("Fatal error initiating playback extraction", e)
                _uiState.value = PlayerUiState.Error(
                    title = mediaTitle,
                    message = "Playback initialization failed: ${e.message}",
                    canRetry = true,
                    fallbackEmbedUrl = defaultEmbedUrl
                )
            }
        }
    }

    /**
     * Mid-playback token expiration recovery: saves current position, fetches fresh .m3u8, and resumes.
     */
    fun recoverExpiredToken(currentPositionMs: Long, context: Context) {
        AppLogger.player("⚠️ Mid-playback token expired at ${currentPositionMs}ms. Auto-refreshing stream...")
        loadMedia(tmdbId, mediaType, season, episode, context, resumePositionMs = currentPositionMs)
    }

    /**
     * Auto-Next TV Episode handler
     */
    fun loadNextEpisode(context: Context) {
        val currentEpNum = episode ?: 0
        val nextEp = _availableEpisodes.value.firstOrNull { it.episodeNumber > currentEpNum }
        if (nextEp != null) {
            AppLogger.player("⏩ Advancing to next episode: S${season}:E${nextEp.episodeNumber} - ${nextEp.name}")
            loadMedia(tmdbId, mediaType, season, nextEp.episodeNumber, context, resumePositionMs = 0L)
        }
    }

    fun moveToNextStream(): StreamLink? {
        val manager = fallbackManager ?: return null
        if (manager.hasMoreStreams()) {
            val next = manager.moveToNextStream()
            if (next != null) {
                _activeStream.value = next
                val state = _uiState.value
                if (state is PlayerUiState.Ready) {
                    _uiState.value = state.copy(
                        activeStream = next,
                        isBuffering = true
                    )
                }
                return next
            }
        }
        return null
    }

    fun switchServer(stream: StreamLink) {
        val manager = fallbackManager ?: return
        manager.moveToStream(stream.serverId)
        _activeStream.value = stream
        val state = _uiState.value
        if (state is PlayerUiState.Ready) {
            _uiState.value = state.copy(
                activeStream = stream,
                isBuffering = true
            )
        }
    }

    fun togglePlaybackSpeed() {
        val nextSpeed = when (_playbackSpeed.value) {
            1.0f -> 1.25f
            1.25f -> 1.5f
            1.5f -> 0.75f
            else -> 1.0f
        }
        _playbackSpeed.value = nextSpeed
        val state = _uiState.value
        if (state is PlayerUiState.Ready) {
            _uiState.value = state.copy(playbackSpeed = nextSpeed)
        }
    }

    fun toggleAspectRatio() {
        val nextMode = when (_aspectRatioMode.value) {
            "Fit" -> "Fill"
            "Fill" -> "Zoom"
            else -> "Fit"
        }
        _aspectRatioMode.value = nextMode
        val state = _uiState.value
        if (state is PlayerUiState.Ready) {
            _uiState.value = state.copy(aspectRatioMode = nextMode)
        }
    }

    fun adjustSubtitleDelay(deltaMs: Long) {
        _subtitleDelayMs.value += deltaMs
        AppLogger.player("Subtitle delay offset: ${_subtitleDelayMs.value}ms")
    }

    fun updateBuffering(buffering: Boolean) {
        val state = _uiState.value
        if (state is PlayerUiState.Ready) {
            _uiState.value = state.copy(isBuffering = buffering)
        }
    }

    fun updatePlaying(playing: Boolean) {
        val state = _uiState.value
        if (state is PlayerUiState.Ready) {
            _uiState.value = state.copy(isPlaying = playing)
        }
    }

    fun updateDuration(durationMs: Long) {
        val state = _uiState.value
        if (state is PlayerUiState.Ready) {
            _uiState.value = state.copy(durationMs = durationMs)
        }
    }

    fun saveResume(positionMs: Long, durationMs: Long) {
        if (positionMs > 0 && tmdbId.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    repository.savePlaybackResume(
                        PlaybackResume(
                            tmdbId = tmdbId,
                            seasonNumber = season ?: 0,
                            episodeNumber = episode ?: 0,
                            positionMs = positionMs,
                            durationMs = durationMs,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun getResumePosition(): Long = withContext(Dispatchers.IO) {
        try {
            repository.getPlaybackResume(tmdbId, season ?: 0, episode ?: 0)?.positionMs ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    fun advanceToNextProvider() {
        AppLogger.player("⏭️ Advancing to next streaming provider...")
        val fallbackUrl = availableEmbedServers.firstOrNull()?.embedUrl ?: ""
        _uiState.value = PlayerUiState.Sniffing(
            title = mediaTitle,
            subtitleInfo = subtitleInfo,
            serverName = "Switching Provider",
            statusMessage = "Advancing to next provider...",
            fallbackEmbedUrl = fallbackUrl
        )
        streamExtractor?.advanceToNextCandidate()
    }

    fun skipCurrentServer() {
        AppLogger.player("⏭️ User skipped server candidate")
        val state = _uiState.value
        if (state is PlayerUiState.Ready) {
            advanceToNextProvider()
        } else {
            streamExtractor?.skipCurrentCandidate()
        }
    }

    fun resetPlayer() {
        AppLogger.player("🧹 Resetting player state and releasing background extractor")
        loadMediaJob?.cancel()
        loadMediaJob = null
        _extractorWebView.value = null
        streamExtractor?.release()
        streamExtractor = null
        _activeStream.value = null
        _availableStreams.value = emptyList()
        _subtitles.value = emptyList()
        _audioTracks.value = emptyList()
        _uiState.value = PlayerUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        resetPlayer()
    }
}
