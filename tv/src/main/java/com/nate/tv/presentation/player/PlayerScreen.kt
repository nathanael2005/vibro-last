package com.nate.tv.presentation.player

import android.app.Activity
import android.net.Uri
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import com.nate.core.domain.model.SubtitleLink
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.dnsoverhttps.DnsOverHttps
import java.io.File
import java.io.FileOutputStream
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.nate.core.common.AppLogger
import com.nate.core.common.theme.ThemeTokens
import com.nate.tv.ui.components.TvCircularProgressIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Production-Ready 100% Native Media3/ExoPlayer Screen.
 *
 * Fully integrated with:
 * - Ripple-free screen touch toggle & D-Pad navigation
 * - Immersive edge-to-edge fullscreen (WindowInsetsControllerCompat)
 * - Complete ExoPlayer lifecycle disposal
 * - Jitter-free timeline scrubber
 * - Hardware track compatibility filtering (isTrackSupported)
 * - Exact TrackSelectionOverrides for Subtitles and Audio
 * - Dynamic AspectRatio in AndroidView.update (Fit/Fill/Zoom)
 * - Clamped seeking and auto-next TV episodes
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    tmdbId: String,
    mediaType: String,
    season: Int?,
    episode: Int?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    val availableStreams by viewModel.availableStreams.collectAsState()
    val availableEpisodes by viewModel.availableEpisodes.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val aspectRatioMode by viewModel.aspectRatioMode.collectAsState()
    val subtitleDelayMs by viewModel.subtitleDelayMs.collectAsState()
    val extractorWebView by viewModel.extractorWebView.collectAsState()
    val liveLogs by AppLogger.liveLogs.collectAsState()

    var showWebInspector by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var isPlayingState by remember { mutableStateOf(true) }
    var isBufferingState by remember { mutableStateOf(true) }
    var isScrubbing by remember { mutableStateOf(false) }
    var seekFeedback by remember { mutableStateOf<String?>(null) }
    var userActivityTrigger by remember { mutableIntStateOf(0) }

    // Dynamic Track Lists extracted from active stream
    var dynamicSubtitles by remember { mutableStateOf<List<SubtitleTrack>>(emptyList()) }
    var dynamicAudioTracks by remember { mutableStateOf<List<AudioTrack>>(emptyList()) }

    // Dialog Visibility States
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }
    var showEpisodesDialog by remember { mutableStateOf(false) }
    var currentQuality by remember { mutableStateOf("Auto (1080p)") }

    val isAnyDialogOpen = showSettingsDialog || showQualityDialog || showSubtitleDialog ||
            showAudioDialog || showServerDialog || showEpisodesDialog

    val rootFocusRequester = remember { FocusRequester() }
    val controlsFocusRequester = remember { FocusRequester() }

    // Intercept Back Button: close web inspector -> close dialogs -> hide controls -> exit
    BackHandler(enabled = showWebInspector || isAnyDialogOpen || showControls) {
        if (showWebInspector) {
            showWebInspector = false
        } else if (isAnyDialogOpen) {
            showSettingsDialog = false
            showQualityDialog = false
            showSubtitleDialog = false
            showAudioDialog = false
            showServerDialog = false
            showEpisodesDialog = false
        } else if (showControls) {
            showControls = false
        }
    }

    // 1. Immersive Fullscreen Mode & Screen Keep-Awake
    val activity = context as? Activity
    DisposableEffect(Unit) {
        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val insetsController = if (window != null) WindowCompat.getInsetsController(window, window.decorView) else null
        insetsController?.hide(WindowInsetsCompat.Type.systemBars())
        insetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // 2. Lifecycle Cleanup on Exit / Media Switch
    DisposableEffect(tmdbId, mediaType, season, episode) {
        onDispose {
            viewModel.resetPlayer()
        }
    }

    // 3. Load media on first compose / key change
    LaunchedEffect(tmdbId, mediaType, season, episode) {
        viewModel.loadMedia(tmdbId, mediaType, season, episode, context)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 0. Active Hidden/Visible Surface for Headless Extractor WebView
        // Keeps Chromium alive on Android TV Leanback so DOM timers, requestAnimationFrame & network run
        // Rendered at full 1080p (fillMaxSize) so HTML5 players and responsive scripts initialize correctly.
        extractorWebView?.let { wv ->
            AndroidView(
                factory = { ctx ->
                    try {
                        (wv.parent as? android.view.ViewGroup)?.removeView(wv)
                        wv.isFocusable = showWebInspector
                        wv.isFocusableInTouchMode = showWebInspector
                        if (!showWebInspector) wv.clearFocus()
                        wv
                    } catch (_: Exception) {
                        android.view.View(ctx)
                    }
                },
                update = { view ->
                    try {
                        view.isFocusable = showWebInspector
                        view.isFocusableInTouchMode = showWebInspector
                        if (!showWebInspector) view.clearFocus()
                    } catch (_: Exception) {}
                },
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (showWebInspector) 1.0f else 0.001f)
                    .focusProperties { canFocus = showWebInspector }
            )
        }

        when (val state = uiState) {
            // ── 0. Idle State: Clean Slate between Episodes / Movies ────────────────
            is PlayerUiState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    TvCircularProgressIndicator(
                        color = ThemeTokens.FocusGold,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            // ── 1. Sniffing State: Background Headless Stream Extraction ─────────────
            is PlayerUiState.Sniffing -> {
                if (showWebInspector) {
                    // Floating Inspector HUD at bottom of screen
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xEE0B1120))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔍 Web Inspector Active",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Button(
                                onClick = { showWebInspector = false },
                                colors = ButtonDefaults.colors(
                                    containerColor = Color(0xFF3B82F6),
                                    focusedContainerColor = ThemeTokens.FocusGold
                                ),
                                shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                            ) {
                                Text("👁️ Hide Inspector", fontSize = 12.sp, color = Color.White)
                            }

                            Button(
                                onClick = { viewModel.skipCurrentServer() },
                                colors = ButtonDefaults.colors(
                                    containerColor = Color(0xFF1E293B),
                                    focusedContainerColor = ThemeTokens.FocusGold
                                ),
                                shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                            ) {
                                Text("⏭️ Skip Server", fontSize = 12.sp, color = Color.White)
                            }

                            Button(
                                onClick = onBack,
                                colors = ButtonDefaults.colors(
                                    containerColor = Color(0xFF1E293B),
                                    focusedContainerColor = Color(0xFFDC2626)
                                ),
                                shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                            ) {
                                Text("Cancel", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 48.dp, vertical = 20.dp)
                        ) {
                            TvCircularProgressIndicator(
                                color = ThemeTokens.FocusGold,
                                modifier = Modifier.size(44.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = state.title,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (!state.subtitleInfo.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = state.subtitleInfo,
                                    color = ThemeTokens.TextGrey,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ThemeTokens.SurfaceDarkBlue)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "⚡ ${state.serverName}",
                                    color = ThemeTokens.FocusGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = state.statusMessage,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            val sniffingCancelFocusRequester = remember { FocusRequester() }
                            LaunchedEffect(Unit) {
                                delay(150L)
                                try {
                                    sniffingCancelFocusRequester.requestFocus()
                                } catch (_: Exception) {}
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = onBack,
                                    modifier = Modifier.focusRequester(sniffingCancelFocusRequester),
                                    colors = ButtonDefaults.colors(
                                        containerColor = Color(0xFF1E293B),
                                        focusedContainerColor = Color(0xFFDC2626)
                                    ),
                                    shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                                ) {
                                    Text("Cancel", fontSize = 12.sp, color = Color.White)
                                }

                                Button(
                                    onClick = { showWebInspector = true },
                                    colors = ButtonDefaults.colors(
                                        containerColor = Color(0xFF1E293B),
                                        focusedContainerColor = ThemeTokens.FocusGold
                                    ),
                                    shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                                ) {
                                    Text("👁️ Web Inspector", fontSize = 12.sp, color = Color.White)
                                }

                                Button(
                                    onClick = { viewModel.skipCurrentServer() },
                                    colors = ButtonDefaults.colors(
                                        containerColor = Color(0xFF1E293B),
                                        focusedContainerColor = ThemeTokens.FocusGold
                                    ),
                                    shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                                ) {
                                    Text("⏭️ Skip Server", fontSize = 12.sp, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            LiveDebugTerminal(
                                logs = liveLogs,
                                modifier = Modifier
                                    .fillMaxWidth(0.92f)
                                    .height(180.dp)
                            )
                        }
                    }
                }
            }

            // ── 2. Ready State: 100% Native Media3/ExoPlayer Playback ────────────────
            is PlayerUiState.Ready -> {
                val readyState = state

                val okHttpClient = remember(readyState.activeStream.url) {
                    val bootstrap = OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).build()
                    val dohDns = try {
                        DnsOverHttps.Builder()
                            .client(bootstrap)
                            .url("https://dns.google/dns-query".toHttpUrl())
                            .bootstrapDnsHosts(
                                listOf(
                                    InetAddress.getByName("8.8.8.8"),
                                    InetAddress.getByName("8.8.4.4")
                                )
                            )
                            .build()
                    } catch (_: Exception) {
                        null
                    }

                    OkHttpClient.Builder()
                        .apply { dohDns?.let { dns(it) } }
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .followRedirects(true)
                        .followSslRedirects(true)
                        .addInterceptor { chain ->
                            val request = chain.request()
                            val url = request.url.toString()
                            val lower = url.lowercase()

                            // If it's a subtitle file, strip restrictive Referer/Origin headers that trigger CloudFront 403
                            if (lower.contains(".srt") || lower.contains(".vtt") || lower.contains("subtitle") || lower.contains("/captions/")) {
                                val strippedRequest = request.newBuilder()
                                    .removeHeader("Referer")
                                    .removeHeader("referer")
                                    .removeHeader("Origin")
                                    .removeHeader("origin")
                                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36")
                                    .build()
                                chain.proceed(strippedRequest)
                            } else {
                                chain.proceed(request)
                            }
                        }
                        .build()
                }

                val exoPlayer = remember(readyState.activeStream.url) {
                    val headers = readyState.activeStream.headers ?: emptyMap()
                    val userAgent = headers["User-Agent"] ?: headers["user-agent"]
                        ?: "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"

                    val okHttpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
                        .setUserAgent(userAgent)

                    if (headers.isNotEmpty()) {
                        okHttpDataSourceFactory.setDefaultRequestProperties(headers)
                    }

                    val dataSourceFactory = DefaultDataSource.Factory(context, okHttpDataSourceFactory)
                    val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

                    val loadControl = DefaultLoadControl.Builder()
                        .setBufferDurationsMs(15000, 45000, 2000, 3500)
                        .setPrioritizeTimeOverSizeThresholds(true)
                        .setBackBuffer(10000, true)
                        .build()

                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                        .build()

                    ExoPlayer.Builder(context)
                        .setMediaSourceFactory(mediaSourceFactory)
                        .setLoadControl(loadControl)
                        .setAudioAttributes(audioAttributes, true)
                        .setHandleAudioBecomingNoisy(true)
                        .setSeekBackIncrementMs(10000)
                        .setSeekForwardIncrementMs(10000)
                        .build()
                }

                // MediaSession for Bluetooth headsets, lock-screen, and TV remotes
                val mediaSession = remember(exoPlayer) {
                    try {
                        MediaSession.Builder(context, exoPlayer).build()
                    } catch (_: Exception) {
                        null
                    }
                }

                DisposableEffect(mediaSession) {
                    onDispose {
                        mediaSession?.release()
                    }
                }

                // Hardware Lifecycle Observer: Pause on background, resume on foreground
                DisposableEffect(lifecycleOwner, exoPlayer) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                            exoPlayer.pause()
                            viewModel.saveResume(exoPlayer.currentPosition, exoPlayer.duration)
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        viewModel.saveResume(exoPlayer.currentPosition, exoPlayer.duration)
                        exoPlayer.stop()
                        exoPlayer.clearMediaItems()
                        exoPlayer.release()
                    }
                }

                // Initial Resume Position Guard
                var hasResumedInitialPosition by remember(readyState.activeStream.url) { mutableStateOf(false) }

                // Player Listener & Dynamic Track Extraction
                DisposableEffect(exoPlayer) {
                    val listener = object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            isPlayingState = isPlaying
                            viewModel.updatePlaying(isPlaying)
                        }

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            val buffering = playbackState == Player.STATE_BUFFERING
                            isBufferingState = buffering
                            viewModel.updateBuffering(buffering)

                            if (playbackState == Player.STATE_READY) {
                                val dur = exoPlayer.duration.coerceAtLeast(0)
                                durationMs = dur
                                viewModel.updateDuration(dur)

                                if (!hasResumedInitialPosition) {
                                    val savedPos = readyState.initialPositionMs
                                    if (savedPos > 0L) {
                                        exoPlayer.seekTo(savedPos)
                                        AppLogger.player("Resumed playback at saved position: ${savedPos}ms")
                                    }
                                    hasResumedInitialPosition = true
                                }
                            }

                            // Auto-advance to next episode when video completes
                            if (playbackState == Player.STATE_ENDED) {
                                viewModel.loadNextEpisode(context)
                            }
                        }

                        override fun onTracksChanged(tracks: Tracks) {
                            val subTracks = mutableListOf<SubtitleTrack>()
                            val audTracks = mutableListOf<AudioTrack>()

                            for (group in tracks.groups) {
                                val type = group.type
                                for (i in 0 until group.length) {
                                    // Text tracks are software-rendered, exempt from hardware decoder support check
                                    if (type != C.TRACK_TYPE_TEXT && !group.isTrackSupported(i)) continue

                                    val format = group.getTrackFormat(i)
                                    val isSelected = group.isTrackSelected(i)

                                    if (type == C.TRACK_TYPE_TEXT) {
                                        val label = format.label ?: format.language ?: "Subtitle ${subTracks.size + 1}"
                                        subTracks.add(
                                            SubtitleTrack(
                                                id = format.language ?: "$i",
                                                label = label,
                                                lang = format.language ?: "und",
                                                isSelected = isSelected,
                                                trackGroup = group.mediaTrackGroup,
                                                trackIndex = i
                                            )
                                        )
                                    } else if (type == C.TRACK_TYPE_AUDIO) {
                                        val label = format.label ?: format.language ?: "Audio ${audTracks.size + 1}"
                                        audTracks.add(
                                            AudioTrack(
                                                index = i,
                                                label = label,
                                                language = format.language ?: "und",
                                                isSelected = isSelected,
                                                trackGroup = group.mediaTrackGroup,
                                                trackIndex = i
                                            )
                                        )
                                    }
                                }
                            }

                            dynamicSubtitles = subTracks
                            dynamicAudioTracks = audTracks
                            AppLogger.player("Tracks updated: ${subTracks.size} subtitles (${subTracks.count { it.isSelected }} active), ${audTracks.size} audio streams")
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            AppLogger.error("ExoPlayer playback error: ${error.message}", error)
                            val currentPos = exoPlayer.currentPosition
                            if (currentPos > 2000L) {
                                AppLogger.player("Token expired during playback at ${currentPos}ms. Refreshing token...")
                                viewModel.recoverExpiredToken(currentPos, context)
                            } else {
                                AppLogger.player("⚠️ Stream failed on startup (${error.message}). Advancing to next provider...")
                                val next = viewModel.moveToNextStream()
                                if (next == null) {
                                    viewModel.advanceToNextProvider()
                                }
                            }
                        }
                    }
                    exoPlayer.addListener(listener)
                    onDispose {
                        exoPlayer.removeListener(listener)
                    }
                }

                // Load and Prepare Stream with Subtitle Configurations
                LaunchedEffect(readyState.activeStream) {
                    val stream = readyState.activeStream
                    val mimeType = when {
                        stream.url.contains(".m3u8", ignoreCase = true) || stream.format.equals("hls", ignoreCase = true) -> MimeTypes.APPLICATION_M3U8
                        stream.url.contains(".mpd", ignoreCase = true) || stream.format.equals("dash", ignoreCase = true) -> MimeTypes.APPLICATION_MPD
                        stream.url.contains(".mp4", ignoreCase = true) -> MimeTypes.VIDEO_MP4
                        else -> null
                    }

                    // Attach sidecar subtitles
                    val subtitleConfigs = stream.subtitles.map { sub ->
                        val lower = sub.url.lowercase()
                        val isSrt = sub.format.equals("SRT", ignoreCase = true) || lower.contains(".srt")
                        val subMime = if (isSrt) MimeTypes.APPLICATION_SUBRIP else MimeTypes.TEXT_VTT

                        MediaItem.SubtitleConfiguration.Builder(Uri.parse(sub.url))
                            .setMimeType(subMime)
                            .setLanguage(sub.lang)
                            .setLabel(sub.label)
                            .setRoleFlags(C.ROLE_FLAG_SUBTITLE)
                            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                            .build()
                    }

                    val mediaItem = MediaItem.Builder()
                        .setUri(stream.url)
                        .apply { if (mimeType != null) setMimeType(mimeType) }
                        .setSubtitleConfigurations(subtitleConfigs)
                        .build()

                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)

                    exoPlayer.prepare()
                    exoPlayer.playWhenReady = true
                }

                LaunchedEffect(playbackSpeed) {
                    exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
                }

                // 3. Jitter-Free Timeline Position Tracker
                LaunchedEffect(isPlayingState, isScrubbing) {
                    while (isActive && isPlayingState && !isScrubbing) {
                        currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                        if (exoPlayer.duration > 0) {
                            durationMs = exoPlayer.duration
                        }
                        delay(500L)
                    }
                }

                // 4. Auto-hide controls timeout (4s)
                LaunchedEffect(showControls, userActivityTrigger, isPlayingState, isAnyDialogOpen) {
                    if (showControls && !isAnyDialogOpen && isPlayingState) {
                        delay(4000L)
                        showControls = false
                    }
                }

                // 4b. Focus management between Player Controls and Root Container on Android TV
                LaunchedEffect(showControls, isAnyDialogOpen) {
                    delay(80L)
                    if (showControls && !isAnyDialogOpen) {
                        try {
                            controlsFocusRequester.requestFocus()
                        } catch (_: Exception) {}
                    } else if (!showControls) {
                        try {
                            rootFocusRequester.requestFocus()
                        } catch (_: Exception) {}
                    }
                }

                LaunchedEffect(seekFeedback) {
                    if (seekFeedback != null) {
                        delay(1200L)
                        seekFeedback = null
                    }
                }

                val playbackActions = remember(onBack) {
                    PlaybackActions(
                        onPlayPause = {
                            userActivityTrigger++
                            if (exoPlayer.isPlaying) {
                                exoPlayer.pause()
                            } else {
                                exoPlayer.play()
                            }
                        },
                        onSeekForward = {
                            userActivityTrigger++
                            seekFeedback = "⏩ +10s"
                            val dur = exoPlayer.duration
                            val target = if (dur > 0) minOf(dur, exoPlayer.currentPosition + 10000L) else exoPlayer.currentPosition + 10000L
                            exoPlayer.seekTo(target)
                        },
                        onSeekBackward = {
                            userActivityTrigger++
                            seekFeedback = "⏪ -10s"
                            val target = maxOf(0L, exoPlayer.currentPosition - 10000L)
                            exoPlayer.seekTo(target)
                        },
                        onOpenSettings = {
                            userActivityTrigger++
                            showSettingsDialog = true
                        },
                        onOpenServerSelector = {
                            userActivityTrigger++
                            showServerDialog = true
                        },
                        onOpenSubtitles = {
                            userActivityTrigger++
                            showSubtitleDialog = true
                        },
                        onOpenAudioTracks = {
                            userActivityTrigger++
                            showAudioDialog = true
                        },
                        onOpenQuality = {
                            userActivityTrigger++
                            showQualityDialog = true
                        },
                        onOpenEpisodesSheet = {
                            userActivityTrigger++
                            showEpisodesDialog = true
                        },
                        onNextEpisode = {
                            userActivityTrigger++
                            viewModel.loadNextEpisode(context)
                        },
                        onToggleAspectRatio = {
                            userActivityTrigger++
                            viewModel.toggleAspectRatio()
                        },
                        onToggleSpeed = {
                            userActivityTrigger++
                            viewModel.togglePlaybackSpeed()
                            exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
                        },
                        onCancelCountdown = {},
                        onBack = onBack
                    )
                }

                // 5. Root Screen Container with Touch Toggle & TV D-Pad navigation
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            userActivityTrigger++
                            showControls = !showControls
                        }
                        .focusRequester(rootFocusRequester)
                        .focusable()
                        .onPreviewKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown) {
                                val keyCode = event.nativeKeyEvent.keyCode

                                // Dedicated Remote Hardware Media Keys: always handle immediately
                                when (keyCode) {
                                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                                    KeyEvent.KEYCODE_MEDIA_PLAY,
                                    KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                                        userActivityTrigger++
                                        playbackActions.onPlayPause()
                                        return@onPreviewKeyEvent true
                                    }
                                    KeyEvent.KEYCODE_MEDIA_REWIND -> {
                                        userActivityTrigger++
                                        showControls = true
                                        playbackActions.onSeekBackward()
                                        return@onPreviewKeyEvent true
                                    }
                                    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                                        userActivityTrigger++
                                        showControls = true
                                        playbackActions.onSeekForward()
                                        return@onPreviewKeyEvent true
                                    }
                                }

                                // When controls are HIDDEN, any D-Pad action awakens the controls overlay
                                if (!showControls) {
                                    userActivityTrigger++
                                    when (keyCode) {
                                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                                            showControls = true
                                            true
                                        }
                                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                                            showControls = true
                                            playbackActions.onSeekBackward()
                                            true
                                        }
                                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                            showControls = true
                                            playbackActions.onSeekForward()
                                            true
                                        }
                                        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> {
                                            showControls = true
                                            true
                                        }
                                        else -> false
                                    }
                                } else {
                                    // When controls ARE VISIBLE:
                                    // Refresh auto-hide timeout on interaction
                                    userActivityTrigger++
                                    // Do NOT intercept D-Pad keys so focused child buttons can navigate and click!
                                    false
                                }
                            } else false
                        }
                        .onKeyEvent { event ->
                            // Fallback if event reaches root container without child consuming it
                            if (event.type == KeyEventType.KeyDown && showControls) {
                                when (event.nativeKeyEvent.keyCode) {
                                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                                        playbackActions.onPlayPause()
                                        true
                                    }
                                    else -> false
                                }
                            } else false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Video Surface (Base of Z-Index)
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .focusProperties { canFocus = false },
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                useController = false
                                player = exoPlayer
                                keepScreenOn = true
                                isFocusable = false
                                isFocusableInTouchMode = false
                                descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
                                resizeMode = when (aspectRatioMode) {
                                    "Fill" -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                    "Zoom" -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                    else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                            }
                        },
                        update = { playerView ->
                            playerView.isFocusable = false
                            playerView.isFocusableInTouchMode = false
                            playerView.descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
                            playerView.resizeMode = when (aspectRatioMode) {
                                "Fill" -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                "Zoom" -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                            }
                        }
                    )

                    // Centered Buffering Spinner
                    if (isBufferingState) {
                        TvCircularProgressIndicator(
                            color = ThemeTokens.FocusGold,
                            modifier = Modifier
                                .size(64.dp)
                                .align(Alignment.Center)
                        )
                    }

                    // Unified Player Controls Overlay (Animated on Top)
                    AnimatedVisibility(
                        visible = showControls || seekFeedback != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        PlayerControls(
                            title = readyState.title,
                            subtitleInfo = readyState.subtitleInfo,
                            serverName = readyState.activeStream.serverName,
                            posterUrl = viewModel.posterUrl,
                            positionMs = currentPositionMs,
                            durationMs = durationMs,
                            isPlaying = isPlayingState,
                            playbackSpeed = playbackSpeed,
                            aspectRatioMode = aspectRatioMode,
                            hasNextEpisode = readyState.hasNextEpisode,
                            hasEpisodes = availableEpisodes.isNotEmpty(),
                            seekFeedback = seekFeedback,
                            nextEpisodeCountdown = readyState.nextEpisodeCountdown,
                            actions = playbackActions,
                            controlsFocusRequester = controlsFocusRequester
                        )
                    }
                }

                // Native Dialog Overlays
                if (showSettingsDialog) {
                    SettingsDialog(
                        playbackSpeed = playbackSpeed,
                        aspectRatioMode = aspectRatioMode,
                        onToggleSpeed = { playbackActions.onToggleSpeed() },
                        onToggleAspectRatio = { playbackActions.onToggleAspectRatio() },
                        onOpenWebPlayer = {
                            viewModel.loadMedia(tmdbId, mediaType, season, episode, context)
                            showSettingsDialog = false
                        },
                        onDismiss = { showSettingsDialog = false }
                    )
                }

                if (showQualityDialog) {
                    QualityDialog(
                        currentQuality = currentQuality,
                        onSelectQuality = { quality ->
                            currentQuality = quality
                            val (w, h) = when {
                                quality.contains("1080") -> 1920 to 1080
                                quality.contains("720") -> 1280 to 720
                                quality.contains("480") -> 854 to 480
                                else -> Int.MAX_VALUE to Int.MAX_VALUE
                            }
                            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                .buildUpon()
                                .setMaxVideoSize(w, h)
                                .build()
                            AppLogger.player("Quality switched to: $quality (${w}x${h})")
                            showQualityDialog = false
                        },
                        onDismiss = { showQualityDialog = false }
                    )
                }

                if (showSubtitleDialog) {
                    val displaySubtitles = if (dynamicSubtitles.isNotEmpty()) {
                        dynamicSubtitles
                    } else {
                        readyState.activeStream.subtitles.mapIndexed { idx, sub ->
                            SubtitleTrack(
                                id = sub.lang,
                                label = sub.label,
                                lang = sub.lang,
                                isSelected = false
                            )
                        }
                    }

                    SubtitleSelectorDialog(
                        subtitles = displaySubtitles,
                        onSelect = { track ->
                            val builder = exoPlayer.trackSelectionParameters.buildUpon()
                            if (track == null) {
                                builder.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                                AppLogger.player("Subtitle disabled")
                            } else {
                                builder.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                if (track.trackGroup != null && track.trackIndex != -1) {
                                    builder.setOverrideForType(TrackSelectionOverride(track.trackGroup, track.trackIndex))
                                } else {
                                    builder.setPreferredTextLanguage(track.lang)
                                }
                                AppLogger.player("Subtitle selected: ${track.label} (${track.lang})")
                            }
                            exoPlayer.trackSelectionParameters = builder.build()
                            showSubtitleDialog = false
                        },
                        onDismiss = { showSubtitleDialog = false }
                    )
                }

                if (showAudioDialog) {
                    AudioTrackSelectorDialog(
                        audioTracks = dynamicAudioTracks,
                        onSelect = { track ->
                            if (track.trackGroup != null && track.trackIndex != -1) {
                                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                    .buildUpon()
                                    .setOverrideForType(TrackSelectionOverride(track.trackGroup, track.trackIndex))
                                    .build()
                            }
                            AppLogger.player("Audio track selected: ${track.label}")
                            showAudioDialog = false
                        },
                        onDismiss = { showAudioDialog = false }
                    )
                }

                if (showServerDialog) {
                    ServerSelectorDialog(
                        streams = availableStreams,
                        activeStream = readyState.activeStream,
                        onSelect = { selectedStream ->
                            viewModel.switchServer(selectedStream)
                            showServerDialog = false
                        },
                        onDismiss = { showServerDialog = false }
                    )
                }

                if (showEpisodesDialog) {
                    EpisodesSheetDialog(
                        episodes = availableEpisodes,
                        currentEpisodeNumber = episode,
                        onSelectEpisode = { selectedEp ->
                            viewModel.loadMedia(tmdbId, mediaType, season, selectedEp.episodeNumber, context)
                            showEpisodesDialog = false
                        },
                        onDismiss = { showEpisodesDialog = false }
                    )
                }
            }

            // ── 3. Error State ───────────────────────────────────────────────────────
            is PlayerUiState.Error -> {
                if (showWebInspector && extractorWebView != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xEE0B1120))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔍 Inspecting Failed Page",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Button(
                                onClick = { showWebInspector = false },
                                colors = ButtonDefaults.colors(
                                    containerColor = Color(0xFF3B82F6),
                                    focusedContainerColor = ThemeTokens.FocusGold
                                ),
                                shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                            ) {
                                Text("👁️ Hide Inspector", fontSize = 12.sp, color = Color.White)
                            }

                            Button(
                                onClick = onBack,
                                colors = ButtonDefaults.colors(
                                    containerColor = Color(0xFF1E293B),
                                    focusedContainerColor = Color(0xFFDC2626)
                                ),
                                shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                            ) {
                                Text("Back", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 48.dp, vertical = 20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Error",
                                tint = Color.Red,
                                modifier = Modifier.size(44.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = state.title,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = state.message,
                                color = Color.LightGray,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                if (state.canRetry) {
                                    Button(
                                        onClick = { viewModel.loadMedia(tmdbId, mediaType, season, episode, context) },
                                        colors = ButtonDefaults.colors(
                                            containerColor = ThemeTokens.FocusGold,
                                            contentColor = Color.Black
                                        ),
                                        shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                                    ) {
                                        Text("Retry", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }

                                if (extractorWebView != null) {
                                    Button(
                                        onClick = { showWebInspector = true },
                                        colors = ButtonDefaults.colors(
                                            containerColor = Color(0xFF1E293B),
                                            focusedContainerColor = ThemeTokens.FocusGold
                                        ),
                                        shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                                    ) {
                                        Text("👁️ Web Inspector", fontSize = 12.sp, color = Color.White)
                                    }
                                }

                                Button(
                                    onClick = onBack,
                                    colors = ButtonDefaults.colors(
                                        containerColor = Color(0xFF1E293B),
                                        contentColor = Color.White
                                    ),
                                    shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                                ) {
                                    Text("Back", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            LiveDebugTerminal(
                                logs = liveLogs,
                                modifier = Modifier
                                    .fillMaxWidth(0.92f)
                                    .height(180.dp)
                            )
                        }
                    }
                }
            }

            // ── 4. Fallback State ───────────────────────────────────────────────────
            is PlayerUiState.WebViewFallback -> {
                LaunchedEffect(Unit) {
                    viewModel.loadMedia(tmdbId, mediaType, season, episode, context)
                }
            }
        }
    }
}

@Composable
private fun LiveDebugTerminal(
    logs: List<String>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xEE0B1120))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📺 Live Diagnostic Terminal (Logs)",
                color = ThemeTokens.FocusGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${logs.size} events",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Awaiting events...",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(logs) { log ->
                    val color = when {
                        log.contains("❌") || log.contains("Error", ignoreCase = true) || log.contains("failed", ignoreCase = true) -> Color(0xFFF87171)
                        log.contains("⚠️") || log.contains("Warning", ignoreCase = true) -> Color(0xFFFBBF24)
                        log.contains("🎬") || log.contains("Ready", ignoreCase = true) || log.contains("Success", ignoreCase = true) -> Color(0xFF4ADE80)
                        log.contains("⚡") -> Color(0xFF60A5FA)
                        log.contains("🌐") || log.contains("http", ignoreCase = true) -> Color(0xFF38BDF8)
                        else -> Color(0xFFCBD5E1)
                    }
                    Text(
                        text = log,
                        color = color,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}
