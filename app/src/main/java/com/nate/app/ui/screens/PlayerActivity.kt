package com.nate.app.ui.screens

import androidx.activity.ComponentActivity
import android.content.Intent
import android.net.Uri
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.InputDevice
import android.view.ViewGroup
import android.view.View
import com.nate.app.R
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.view.Gravity
import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import java.io.ByteArrayInputStream
import kotlinx.coroutines.*
import com.nate.app.util.AdaptiveLoadControl
import com.nate.app.util.SubtitleManager
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import com.nate.app.data.local.PreferencesManager

class PlayerActivity : ComponentActivity() {

    private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var preferencesManager: PreferencesManager
    private var resumePositionMs: Long = 0L
    private val cleanupRunnables = mutableListOf<Runnable>()
    private lateinit var subtitleManager: SubtitleManager

    // Use nullable var instead of lateinit to prevent UninitializedPropertyAccessException crashes
    private var webView: WebView? = null
    private var exoPlayer: ExoPlayer? = null
    private var loadingLayout: LinearLayout? = null
    private var nativePlayerView: PlayerView? = null
    private var usingWebFallback: Boolean = false
    private var currentMediaType: String = "movie"
    private var currentMediaId: Int = 0
    private var currentSeason: Int = 1
    private var currentEpisode: Int = 1
    private var seasonEpisodeCount: Int = 0
    private var controlsOverlay: View? = null
    private var progressSeekBar: SeekBar? = null
    private var currentTimeLabel: TextView? = null
    private var durationLabel: TextView? = null
    private var playPauseButton: ImageButton? = null
    private var pendingEmbedUrl: String? = null
    private var isPlaying: Boolean = true
    private var rootLayoutRef: FrameLayout? = null
    private var streamHandoffStarted: Boolean = false
    private val uiHandler = Handler(Looper.getMainLooper())
    private var lastUiRevealAtMs: Long = 0L
    private val hideControlsRunnable = Runnable {
        controlsOverlay?.visibility = View.GONE
    }
    private val progressTicker = object : Runnable {
        override fun run() {
            if (isFinishing || isDestroyed) return
            val player = exoPlayer
            if (player != null && !usingWebFallback && player.isPlaying) {
                val duration = player.duration.coerceAtLeast(0L)
                val position = player.currentPosition.coerceAtLeast(0L)
                progressSeekBar?.max = duration.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                progressSeekBar?.progress = position.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                currentTimeLabel?.text = formatTime(position)
                durationLabel?.text = formatTime(duration)
                saveCurrentPosition(position, duration)
            } else if (usingWebFallback && webView != null) {
                queryWebViewProgress()
            }
            uiHandler.postDelayed(this, 1000)
        }
    }

    private fun queryWebViewProgress() {
        val wv = webView ?: return
        wv.evaluateJavascript(
            "(function(){var v=document.querySelector('video'); return v ? JSON.stringify({pos: v.currentTime, dur: v.duration}) : null;})();"
        ) { result ->
            if (result != null && result != "null") {
                try {
                    val normalizedResult = if (result.startsWith("\"") && result.endsWith("\"")) {
                        result.substring(1, result.length - 1)
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\")
                    } else {
                        result
                    }
                    val json = org.json.JSONObject(normalizedResult)
                    val posSec = json.optDouble("pos", 0.0)
                    val durSec = json.optDouble("dur", 0.0)
                    if (durSec > 0 && !posSec.isNaN() && !durSec.isNaN()) {
                        val posMs = (posSec * 1000).toLong()
                        val durMs = (durSec * 1000).toLong()
                        runOnUiThread {
                            if (isFinishing || isDestroyed) return@runOnUiThread
                            progressSeekBar?.max = durMs.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                            progressSeekBar?.progress = posMs.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                            currentTimeLabel?.text = formatTime(posMs)
                            durationLabel?.text = formatTime(durMs)
                            saveCurrentPosition(posMs, durMs)
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun saveCurrentPosition(positionMs: Long, durationMs: Long = 0L) {
        if (positionMs <= 0) return
        val isNearEnd = if (durationMs > 0) {
            positionMs >= durationMs - 15_000L || positionMs.toFloat() / durationMs.toFloat() > 0.95f
        } else {
            false
        }
        activityScope.launch(Dispatchers.IO) {
            if (isNearEnd) {
                preferencesManager.clearResumePosition(
                    mediaId = currentMediaId,
                    mediaType = currentMediaType,
                    season = currentSeason,
                    episode = currentEpisode
                )
            } else {
                preferencesManager.saveResumePosition(
                    mediaId = currentMediaId,
                    mediaType = currentMediaType,
                    season = currentSeason,
                    episode = currentEpisode,
                    positionMs = positionMs
                )
            }
        }
    }

    companion object {
        const val EXTRA_MEDIA_ID = "MEDIA_ID"
        const val EXTRA_MEDIA_TYPE = "MEDIA_TYPE"
        const val EXTRA_MEDIA_TITLE = "MEDIA_TITLE"
        const val EXTRA_SEASON = "SEASON"
        const val EXTRA_EPISODE = "EPISODE"
        const val EXTRA_EPISODE_COUNT = "EPISODE_COUNT"
        const val EXTRA_RESUME_POSITION_MS = "RESUME_POSITION_MS"
        const val EXTRA_STREAM_URL = "STREAM_URL"
        const val TAG_AUDIT = "STREAM_AUDIT"
        
        // Multiple providers for better reliability
        val PROVIDERS = listOf(
            "https://player.videasy.net",
            "https://vidsrc.to/embed",
            "https://vidsrc.me/embed",
            "https://embed.su/embed",
            "https://autoembed.to"
        )
        
        const val VIDEASY_BASE = "https://player.videasy.net"
        const val STREAM_USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        // Flip to true locally to validate native Media3 path with a guaranteed raw stream.
        const val FORCE_TEST_RAW_STREAM = false
        const val TEST_RAW_STREAM_URL = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
    }

    private val adDomains = listOf(
        "doubleclick.net", "googleads.g.doubleclick.net", "googlesyndication.com",
        "popads.net", "popcash.net", "exoclick.com", "adsterra.com", "yepads.com",
        "propellerads.com", "juicyads.com", "ad-score.com", "onclickads.net",
        "adservice.google.com", "aads.hn", "trafficforce.com",
        "clickadu.com", "adform.net", "adroll.com", "adnxs.com",
        "popunder", "adkeeper", "adsystem", "adbrau", "adcolony", "admob",
        "applovin", "unityads", "vungle", "ironsource", "bidswitch", "openx",
        "rubiconproject", "pubmatic", "criteo", "outbrain", "taboola", "revcontent",
        "mgid", "adblade", "adzerk", "adbox", "nativeads", "vast", "vpaid",
        "analytics", "tracking", "tracker", "histats", "amung.us", "statcounter",
        "adservice", "adtracker", "adserver", "adskeeper",
        "monetization", "monetize", "revenue", "onclick", "highrevenuegate",
        "optmstr.com", "adsco.re", "adtrue.com", "bidgear.com", "adpushup.com",
        "adskeeper.com", "adcash.com", "hilltopads.net", "ad-maven.com", "revenuehits.com",
        "popmyads.com", "adsterra", "clickaine.com", "syndication", "adsafeprotected.com"
    )

    private val adStreamTokens = listOf(
        "preroll", "midroll", "postroll", "advert", "/ad/", "doubleclick",
        "vast", "vpaid", "imasdk", "googlesyndication", "adbreak", "ad-break"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            subtitleManager = SubtitleManager(this)
            preferencesManager = PreferencesManager(applicationContext)
            setupPlayer()
        } catch (t: Throwable) {
            android.util.Log.e("PlayerActivity", "CRASH in setupPlayer", t)
            try {
                showCrashScreen(t)
            } catch (t2: Throwable) {
                android.util.Log.e("PlayerActivity", "CRASH in showCrashScreen", t2)
                // If even the crash screen fails, just finish
                finish()
            }
        }
    }

    private fun setupPlayer() {
        activityScope.launch(Dispatchers.Main) {
            val mediaId = intent.getIntExtra(EXTRA_MEDIA_ID, 0)
            val mediaType = intent.getStringExtra(EXTRA_MEDIA_TYPE) ?: "movie"
            val season = intent.getIntExtra(EXTRA_SEASON, 1)
            val episode = intent.getIntExtra(EXTRA_EPISODE, 1)
            val mediaTitle = intent.getStringExtra(EXTRA_MEDIA_TITLE) ?: "Video"
            seasonEpisodeCount = intent.getIntExtra(EXTRA_EPISODE_COUNT, 0)
            val incomingUrl = intent.getStringExtra(EXTRA_STREAM_URL)

            var resumePos = intent.getLongExtra(EXTRA_RESUME_POSITION_MS, 0L)
            if (resumePos <= 0L) {
                resumePos = withContext(Dispatchers.IO) {
                    preferencesManager.getResumePositionMs(mediaId, mediaType, season, episode)
                }
            }
            resumePositionMs = resumePos

            val streamUrlRaw = resolvePlaybackUrl(
                incomingUrl = incomingUrl,
                mediaType = mediaType,
                mediaId = mediaId,
                season = season,
                episode = episode
            )
            val streamUrl = streamUrlRaw.ifBlank {
                buildEmbedUrl(mediaType, mediaId, season, episode)
            }
            val isRawStream = isRawStreamUrl(streamUrl)
            logRouteDecision(streamUrl, isRawStream)
            currentMediaType = mediaType
            currentMediaId = mediaId
            currentSeason = season
            currentEpisode = episode

            // FLAG_KEEP_SCREEN_ON is safe to set before setContentView
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            // Root layout
            val rootLayout = FrameLayout(this@PlayerActivity).apply {
                setBackgroundColor(Color.BLACK)
            }
            rootLayoutRef = rootLayout

            // Loading overlay
            val ll = LinearLayout(this@PlayerActivity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(Color.parseColor("#060913"))
            }
            loadingLayout = ll

            val loadingText = TextView(this@PlayerActivity).apply {
                text = if (mediaType == "tv") {
                    "Playing: $mediaTitle\nSeason $season Episode $episode"
                } else {
                    "Playing: $mediaTitle"
                }
                textSize = 22f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(32, 0, 32, 32)
            }

            val spinner = ProgressBar(this@PlayerActivity).apply {
                indeterminateTintList = ColorStateList.valueOf(Color.parseColor("#EAB308"))
            }

            ll.addView(loadingText)
            ll.addView(spinner)
            rootLayout.addView(ll)

            setContentView(rootLayout)

            // Fullscreen must be applied AFTER setContentView — DecorView only exists after this point
            window.decorView.post {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        window.insetsController?.let { controller ->
                            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        window.decorView.systemUiVisibility = (
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                        )
                    }
                } catch (t: Throwable) {
                    android.util.Log.w("PlayerActivity", "Fullscreen setup failed (non-fatal)", t)
                }
            }

            // Prefer stable web fallback for Videasy embeds; native path only for confirmed raw files.
            if (isRawStream) {
                try {
                    initNativePlayer(rootLayout, streamUrl, resumePositionMs)
                } catch (t: Throwable) {
                    android.util.Log.e("PlayerActivity", "Native init failed, fallback to WebView", t)
                    initWebFallback(rootLayout, buildEmbedUrl(mediaType, mediaId, season, episode))
                }
            } else {
                initWebFallback(rootLayout, streamUrl)
            }
            try {
                setupOverlayControls(rootLayout, mediaType == "tv")
            } catch (t: Throwable) {
                android.util.Log.e("PlayerActivity", "Overlay init failed (continuing playback)", t)
            }
        }
    }

    private fun showCrashScreen(t: Throwable) {
        android.util.Log.e("PlayerActivity", "Startup error", t)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0F172A"))
            setPadding(48, 48, 48, 48)
        }

        val titleView = TextView(this).apply {
            text = "Unable to start playback"
            textSize = 22f
            setTextColor(Color.WHITE)
            setTypeface(android.graphics.Typeface.DEFAULT_BOLD)
            setPadding(0, 0, 0, 16)
        }

        val messageView = TextView(this).apply {
            text = t.message?.takeIf { it.isNotBlank() }
                ?: "Something went wrong while opening the player. Please try again."
            textSize = 16f
            setTextColor(Color.parseColor("#94A3B8"))
        }

        val backButton = android.widget.Button(this).apply {
            text = "Go Back"
            setOnClickListener { finish() }
        }

        layout.addView(titleView)
        layout.addView(messageView)
        layout.addView(backButton)
        setContentView(layout)
    }

    private fun resolvePlaybackUrl(
        incomingUrl: String?,
        mediaType: String,
        mediaId: Int,
        season: Int,
        episode: Int
    ): String {
        if (FORCE_TEST_RAW_STREAM) return TEST_RAW_STREAM_URL
        if (!incomingUrl.isNullOrBlank()) return incomingUrl
        return buildEmbedUrl(mediaType, mediaId, season, episode)
    }

    private var currentProviderIndex = 0

    private fun buildEmbedUrl(
        mediaType: String,
        mediaId: Int,
        season: Int,
        episode: Int,
        providerIndex: Int = 0
    ): String {
        val base = PROVIDERS.getOrNull(providerIndex) ?: PROVIDERS[0]
        return when {
            base.contains("videasy") -> {
                if (mediaType == "tv") "$base/tv/$mediaId/$season/$episode?overlay=true"
                else "$base/movie/$mediaId?overlay=true"
            }
            base.contains("vidsrc.to") || base.contains("vidsrc.me") -> {
                if (mediaType == "tv") "$base/tv/$mediaId/$season/$episode"
                else "$base/movie/$mediaId"
            }
            base.contains("embed.su") -> {
                if (mediaType == "tv") "$base/tv/$mediaId/$season/$episode"
                else "$base/movie/$mediaId"
            }
            else -> {
                if (mediaType == "tv") "$base/tv/$mediaId/$season/$episode"
                else "$base/movie/$mediaId"
            }
        }
    }

    private fun tryNextProvider() {
        currentProviderIndex++
        if (currentProviderIndex >= PROVIDERS.size) {
            showErrorAndExit("No more sources available for this content.")
            return
        }
        
        val nextUrl = buildEmbedUrl(currentMediaType, currentMediaId, currentSeason, currentEpisode, currentProviderIndex)
        runOnUiThread {
            android.widget.Toast.makeText(this, "Trying Source #${currentProviderIndex + 1}...", android.widget.Toast.LENGTH_SHORT).show()
            val root = rootLayoutRef ?: return@runOnUiThread
            
            cancelScheduledCleanups()
            
            // Cleanup current players
            exoPlayer?.stop()
            exoPlayer?.release()
            exoPlayer = null
            nativePlayerView?.let { (it.parent as? ViewGroup)?.removeView(it) }
            nativePlayerView = null
            
            webView?.let { (it.parent as? ViewGroup)?.removeView(it) }
            webView?.stopLoading()
            webView?.destroy()
            webView = null
            
            usingWebFallback = true
            streamHandoffStarted = false
            initWebFallback(root, nextUrl)
        }
    }

    private fun isRawStreamUrl(url: String): Boolean {
        val parsed = runCatching { Uri.parse(url) }.getOrNull()
        val path = parsed?.path?.lowercase().orEmpty()
        val rawByExtension = path.endsWith(".m3u8") || path.endsWith(".mp4") || path.endsWith(".mpd")
        val rawByQuery = url.contains(".m3u8", ignoreCase = true) || url.contains(".mp4", ignoreCase = true) || url.contains(".mpd", ignoreCase = true)
        return rawByExtension || rawByQuery
    }

    private fun logRouteDecision(url: String, isRaw: Boolean) {
        android.util.Log.d(TAG_AUDIT, "Incoming Playback URL: $url")
        if (isRaw) {
            android.util.Log.d(TAG_AUDIT, "Route: Launching Native Jetpack Media3 ExoPlayer")
        } else {
            android.util.Log.w(TAG_AUDIT, "Route: Not a raw stream, using sandboxed WebView fallback")
        }
    }

    private fun initNativePlayer(rootLayout: FrameLayout, streamUrl: String, resumePosition: Long = 0L, referer: String? = null) {
        usingWebFallback = false
        val pv = PlayerView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.BLACK)
            useController = false
            controllerAutoShow = false
            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            keepScreenOn = true
        }
        nativePlayerView = pv
        rootLayout.addView(pv, 0)

        val headers = mutableMapOf<String, String>()
        referer?.let { headers["Referer"] = it }
        headers["Origin"] = VIDEASY_BASE

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(STREAM_USER_AGENT)
            .setDefaultRequestProperties(headers)

        val bandwidthMeter = DefaultBandwidthMeter.Builder(this).build()
        val loadControl = AdaptiveLoadControl(bandwidthMeter)
        val renderersFactory = DefaultRenderersFactory(this).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
            setMediaCodecSelector(MediaCodecSelector.DEFAULT)
        }

        activityScope.launch {
            val mediaItemBuilder = MediaItem.Builder().setUri(streamUrl)
            
            // Look up subtitle URL passed via Intent extras
            val subtitleUrl = intent.getStringExtra("EXTRA_SUBTITLE_URL") ?: ""
            if (subtitleUrl.isNotEmpty()) {
                val subtitleConfig = subtitleManager.downloadAndPrepareSubtitle(subtitleUrl)
                if (subtitleConfig != null) {
                    mediaItemBuilder.setSubtitleConfigurations(listOf(subtitleConfig))
                }
            }

            val player = ExoPlayer.Builder(this@PlayerActivity)
                .setRenderersFactory(renderersFactory)
                .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
                .setLoadControl(loadControl)
                .setBandwidthMeter(bandwidthMeter)
                .build()
                .also { exo ->
                    pv.player = exo
                    exo.setMediaItem(mediaItemBuilder.build())
                    if (resumePosition > 0L) {
                        exo.seekTo(resumePosition)
                    }
                    exo.prepare()
                    exo.playWhenReady = true
                    isPlaying = true
                    updatePlayPauseIcon()
                    
                    exo.addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == Player.STATE_READY) {
                                loadingLayout?.visibility = View.GONE
                                uiHandler.removeCallbacks(progressTicker)
                                uiHandler.post(progressTicker)
                            }
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            if (!usingWebFallback) {
                                this@PlayerActivity.isPlaying = isPlaying
                                updatePlayPauseIcon()
                            }
                        }

                        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                            android.util.Log.e("PlayerActivity", "ExoPlayer playback error encountered", error)
                            runOnUiThread {
                                val nextUrl = buildEmbedUrl(currentMediaType, currentMediaId, currentSeason, currentEpisode, currentProviderIndex)
                                android.widget.Toast.makeText(
                                    this@PlayerActivity,
                                    "Playback error. Falling back to WebView streaming...",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                
                                exoPlayer?.release()
                                exoPlayer = null
                                nativePlayerView?.let { (it.parent as? ViewGroup)?.removeView(it) }
                                nativePlayerView = null
                                
                                val root = rootLayoutRef ?: return@runOnUiThread
                                initWebFallback(root, nextUrl)
                            }
                        }
                    })
                }
            exoPlayer = player
        }
    }

    private fun initWebFallback(rootLayout: FrameLayout, streamUrl: String) {
        pendingEmbedUrl = streamUrl
        runOnUiThread {
            try {
                if (streamUrl.isBlank()) {
                    showErrorAndExit("Invalid video source URL.")
                    return@runOnUiThread
                }

                usingWebFallback = true
                streamHandoffStarted = false
                val wv = WebView(this)
                wv.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                wv.setBackgroundColor(Color.BLACK)
                wv.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                wv.isFocusable = true
                wv.isFocusableInTouchMode = true
                wv.isClickable = true
                wv.isLongClickable = false
                wv.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) {
                        showControlsTemporarily(force = false)
                        wv.requestFocus()
                    }
                    false
                }
                webView = wv
                rootLayout.addView(wv, 0)
                configureWebView()
                wv.loadUrl(streamUrl)
            } catch (e: Exception) {
                android.util.Log.e("PlayerActivity", "WebView init failed", e)
                showErrorAndExit("Failed to safely initialize streaming engine.")
            }
        }
    }

    private fun configureWebView() {
        val wv = webView ?: return

        val cookieManager = android.webkit.CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(wv, false)
        }

        val settings = wv.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = false
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.loadsImagesAutomatically = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.javaScriptCanOpenWindowsAutomatically = false
        settings.setSupportMultipleWindows(false)
        // Some stream pages rely on mixed subresources; strict block breaks playback.
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        settings.allowFileAccess = false
        settings.allowContentAccess = true
        settings.allowFileAccessFromFileURLs = false
        settings.allowUniversalAccessFromFileURLs = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.safeBrowsingEnabled = true
            wv.setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_BOUND, true)
        }
        settings.userAgentString = STREAM_USER_AGENT

        wv.webChromeClient = object : android.webkit.WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: android.os.Message?
            ): Boolean {
                return false // Block all popup windows
            }
        }

        wv.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return true
                if (url.startsWith("http://", ignoreCase = true) ||
                    url.startsWith("https://", ignoreCase = true)
                ) {
                    for (adDomain in adDomains) {
                        if (url.contains(adDomain, ignoreCase = true)) return true
                    }
                    return false
                }
                return blockExternalRedirect(url)
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url.isNullOrBlank()) return true
                if (url.startsWith("http://", ignoreCase = true) ||
                    url.startsWith("https://", ignoreCase = true)
                ) {
                    for (adDomain in adDomains) {
                        if (url.contains(adDomain, ignoreCase = true)) return true
                    }
                    return false
                }
                return blockExternalRedirect(url)
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url?.toString() ?: return null
                for (adDomain in adDomains) {
                    if (url.contains(adDomain, ignoreCase = true)) {
                        return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream("".toByteArray()))
                    }
                }
                if (isRawStreamUrl(url) && !isLikelyAdStream(url)) {
                    maybeHandoffToNativePlayer(url)
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun onRenderProcessGone(
                view: WebView?,
                detail: RenderProcessGoneDetail?
            ): Boolean {
                android.util.Log.e(
                    "PlayerActivity",
                    "WebView renderer crashed. didCrash=${detail?.didCrash()}"
                )
                if (detail?.didCrash() == true) {
                    view?.let { removeViewFromLayout(it) }
                    webView = null
                    val embedUrl = pendingEmbedUrl
                    if (!embedUrl.isNullOrBlank()) {
                        val root = findViewById<FrameLayout>(android.R.id.content)
                            ?.getChildAt(0) as? FrameLayout
                        if (root != null) {
                            initWebFallback(root, embedUrl)
                        } else {
                            showErrorAndExit("Playback renderer restarted. Please reopen the movie.")
                        }
                    } else {
                        showErrorAndExit("Playback renderer restarted. Please reopen the movie.")
                    }
                } else {
                    loadingLayout?.visibility = View.GONE
                }
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.requestFocus()

                loadingLayout?.animate()
                    ?.alpha(0f)
                    ?.setDuration(600)
                    ?.withEndAction {
                        loadingLayout?.visibility = View.GONE
                    }

                // Strip ad overlays and auto-start the real video element.
                injectStreamCleanup(view)
                scheduleStreamCleanup(view)
                
                if (resumePositionMs > 0L) {
                    seekWebViewToPosition(resumePositionMs)
                }
            }
        }
    }

    private fun seekWebViewToPosition(positionMs: Long) {
        val wv = webView ?: return
        val positionSec = positionMs / 1000.0
        wv.evaluateJavascript(
            """
            (function() {
                try {
                    var v = document.querySelector('video');
                    if (v) {
                        var seekTo = $positionSec;
                        if (v.readyState >= 1) {
                            v.currentTime = seekTo;
                        } else {
                            v.addEventListener('loadedmetadata', function() {
                                v.currentTime = seekTo;
                            });
                            v.currentTime = seekTo;
                        }
                    }
                } catch(e) {}
            })();
            """.trimIndent(),
            null
        )
    }

    private fun injectStreamCleanup(view: WebView?) {
        view?.evaluateJavascript(
            """
            (function() {
                try {
                    var style = document.createElement('style');
                    style.innerHTML = [
                        '.ads-banner,.popup-overlay,#pop-under,.ad-box,',
                        '[class*="ad-container"],[id*="ad-container"],',
                        '[class*="preroll"],[class*="countdown"],[class*="popup-ad"],',
                        'iframe[src*="ads"],iframe[src*="doubleclick"]',
                        '{display:none!important;pointer-events:none!important;}'
                    ].join('');
                    document.head.appendChild(style);

                    document.querySelectorAll('iframe').forEach(function(frame) {
                        var src = (frame.getAttribute('src') || '').toLowerCase();
                        if (src.indexOf('ad') >= 0 || src.indexOf('doubleclick') >= 0) {
                            frame.remove();
                        }
                    });

                    var video = document.querySelector('video');
                    if (video) {
                        video.removeAttribute('disablePictureInPicture');
                        if (video.paused) {
                            video.play().catch(function() {
                                video.muted = true;
                                video.play().catch(function(){});
                            });
                        }
                    }
                } catch (e) {}
            })();
            """.trimIndent(),
            null
        )
    }

    private fun scheduleStreamCleanup(view: WebView?) {
        if (view == null || !usingWebFallback) return
        cancelScheduledCleanups()
        repeat(6) { attempt ->
            val runnable = Runnable {
                if (webView == view && view != null) {
                    injectStreamCleanup(view)
                }
            }
            cleanupRunnables.add(runnable)
            uiHandler.postDelayed(runnable, (attempt + 1) * 1500L)
        }
    }

    private fun cancelScheduledCleanups() {
        cleanupRunnables.forEach { uiHandler.removeCallbacks(it) }
        cleanupRunnables.clear()
    }

    private fun isLikelyAdStream(url: String): Boolean {
        val lower = url.lowercase()
        if (adDomains.any { lower.contains(it) }) return true
        return adStreamTokens.any { lower.contains(it) }
    }

    private fun maybeHandoffToNativePlayer(streamUrl: String) {
        if (streamHandoffStarted || !usingWebFallback) return
        streamHandoffStarted = true
        runOnUiThread {
            val root = rootLayoutRef ?: return@runOnUiThread
            android.util.Log.d(TAG_AUDIT, "Stream sniffed — bypassing WebView ads via ExoPlayer: $streamUrl")
            val currentPos = progressSeekBar?.progress?.toLong() ?: 0L
            webView?.let { removeViewFromLayout(it) }
            webView = null
            initNativePlayer(root, streamUrl, resumePosition = currentPos, referer = pendingEmbedUrl)
        }
    }

    private fun controlVideo(action: String) {
        val player = exoPlayer
        if (player != null && !usingWebFallback) {
            when (action) {
                "play_pause" -> if (player.isPlaying) player.pause() else player.play()
                "play" -> player.play()
                "pause" -> player.pause()
                "rewind" -> player.seekTo((player.currentPosition - 10_000L).coerceAtLeast(0L))
                "fast_forward" -> player.seekTo(player.currentPosition + 10_000L)
                "seek_fwd_30" -> player.seekTo(player.currentPosition + 30_000L)
                "seek_bck_30" -> player.seekTo((player.currentPosition - 30_000L).coerceAtLeast(0L))
                else -> Unit
            }
            return
        }
        val wv = webView ?: return
        val js = when (action) {
            "play_pause"   -> "(function(){var v=document.querySelector('video');if(v){if(v.paused){v.play();}else{v.pause();}}})();"
            "play"         -> "(function(){var v=document.querySelector('video');if(v&&v.paused){v.play();}})();"
            "pause"        -> "(function(){var v=document.querySelector('video');if(v&&!v.paused){v.pause();}})();"
            "rewind"       -> "(function(){var v=document.querySelector('video');if(v){v.currentTime=Math.max(0,v.currentTime-10);}})();"
            "fast_forward" -> "(function(){var v=document.querySelector('video');if(v){v.currentTime=Math.min(v.duration,v.currentTime+10);}})();"
            "vol_up"       -> "(function(){var v=document.querySelector('video');if(v){v.volume=Math.min(1,v.volume+0.1);}})();"
            "vol_down"     -> "(function(){var v=document.querySelector('video');if(v){v.volume=Math.max(0,v.volume-0.1);}})();"
            "seek_fwd_30"  -> "(function(){var v=document.querySelector('video');if(v){v.currentTime=Math.min(v.duration,v.currentTime+30);}})();"
            "seek_bck_30"  -> "(function(){var v=document.querySelector('video');if(v){v.currentTime=Math.max(0,v.currentTime-30);}})();"
            else -> null
        }
        js?.let { wv.evaluateJavascript(it, null) }
    }

    private fun playNextEpisode() {
        if (currentMediaType != "tv") return
        if (seasonEpisodeCount > 0 && currentEpisode >= seasonEpisodeCount) {
            // Check if there's a next season. For now, since we don't have total season count easily here,
            // we'll just increment season and reset episode. If it fails, the user will see an error in WebView/Exo.
            currentSeason += 1
            currentEpisode = 1
            android.widget.Toast.makeText(
                this,
                "Moving to Season $currentSeason, Episode 1",
                android.widget.Toast.LENGTH_SHORT,
            ).show()
        } else {
            currentEpisode += 1
        }
        
        streamHandoffStarted = false
        val nextUrl = buildEmbedUrl(
            mediaType = "tv",
            mediaId = currentMediaId,
            season = currentSeason,
            episode = currentEpisode
        )
        pendingEmbedUrl = nextUrl
        android.util.Log.d(TAG_AUDIT, "Next episode URL: $nextUrl")
        controlsOverlay?.findViewById<TextView>(R.id.player_episode_title)?.text =
            "[S$currentSeason:E$currentEpisode] ${intent.getStringExtra(EXTRA_MEDIA_TITLE) ?: "Now Playing"}"

        val root = rootLayoutRef ?: return
        if (!usingWebFallback) {
            exoPlayer?.release()
            exoPlayer = null
            nativePlayerView?.player = null
            nativePlayerView?.let { (it.parent as? ViewGroup)?.removeView(it) }
            nativePlayerView = null
            initWebFallback(root, nextUrl)
            return
        }
        webView?.loadUrl(nextUrl)
    }

    private fun setupOverlayControls(rootLayout: FrameLayout, isTv: Boolean) {
        val overlay = LayoutInflater.from(this).inflate(R.layout.custom_player_controls, rootLayout, false)
        controlsOverlay = overlay

        val mediaTitle = intent.getStringExtra(EXTRA_MEDIA_TITLE) ?: "Now Playing"
        overlay.findViewById<TextView>(R.id.player_series_title).text = "You're watching"
        overlay.findViewById<TextView>(R.id.player_episode_title).text =
            if (isTv) "[S$currentSeason:E$currentEpisode] $mediaTitle" else mediaTitle

        currentTimeLabel = overlay.findViewById(R.id.txt_current_time)
        durationLabel = overlay.findViewById(R.id.txt_total_time)
        progressSeekBar = overlay.findViewById<SeekBar>(R.id.player_seekbar).apply {
            max = 100
            progress = 0
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val progressValue = seekBar?.progress ?: return
                    val player = exoPlayer
                    if (player != null && !usingWebFallback) {
                        player.seekTo(progressValue.toLong())
                    } else {
                        val wv = webView ?: return
                        wv.evaluateJavascript(
                            "(function(){var v=document.querySelector('video');if(v&&v.duration){v.currentTime=(v.duration*${progressValue / 100.0});}})();",
                            null
                        )
                    }
                }
            })
        }

        playPauseButton = overlay.findViewById(R.id.btn_main_play_pause)
        configureFocusableControl(overlay.findViewById(R.id.btn_back)) { finish() }
        configureFocusableControl(playPauseButton!!) {
            controlVideo("play_pause")
            if (usingWebFallback) {
                isPlaying = !isPlaying
                updatePlayPauseIcon()
            }
        }
        configureFocusableControl(overlay.findViewById(R.id.btn_audio_tracks)) {
            android.widget.Toast.makeText(this, "Audio track selection is coming soon.", android.widget.Toast.LENGTH_SHORT).show()
        }
        configureFocusableControl(overlay.findViewById(R.id.btn_subtitles)) {
            android.widget.Toast.makeText(this, "Subtitle selection is coming soon.", android.widget.Toast.LENGTH_SHORT).show()
        }
        configureFocusableControl(overlay.findViewById(R.id.btn_settings)) {
            tryNextProvider()
        }

        val nextBtn = overlay.findViewById<ImageButton>(R.id.btn_next_episode)
        if (isTv) {
            nextBtn.visibility = View.VISIBLE
            configureFocusableControl(nextBtn) { playNextEpisode() }
        }

        rootLayout.addView(overlay)
        showControlsTemporarily()
    }

    private fun configureFocusableControl(view: View, onClick: () -> Unit) {
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.isClickable = true
        view.setOnClickListener { onClick() }
        view.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN &&
                (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                v.performClick()
                true
            } else {
                false
            }
        }
    }

    private fun updatePlayPauseIcon() {
        playPauseButton?.setImageResource(
            if (isPlaying) R.drawable.ic_pause_circle_filled else R.drawable.ic_play_circle_filled
        )
    }

    private fun blockExternalRedirect(url: String): Boolean {
        return try {
            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            intent.resolveActivity(packageManager) != null
        } catch (_: Exception) {
            true
        }
    }

    private fun removeViewFromLayout(view: WebView) {
        (view.parent as? ViewGroup)?.removeView(view)
        view.destroy()
    }

    private fun showErrorAndExit(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
        finish()
    }

    private fun formatTime(ms: Long): String {
        val totalSec = (ms / 1000L).coerceAtLeast(0L)
        val h = totalSec / 3600L
        val m = (totalSec % 3600L) / 60L
        val s = totalSec % 60L
        return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
    }

    private fun showControlsTemporarily(force: Boolean = true) {
        val now = SystemClock.elapsedRealtime()
        if (!force && (now - lastUiRevealAtMs) < 180L) return
        lastUiRevealAtMs = now
        controlsOverlay?.visibility = View.VISIBLE
        playPauseButton?.requestFocus()
        uiHandler.removeCallbacks(hideControlsRunnable)
        uiHandler.postDelayed(hideControlsRunnable, 5000)
    }

    // Pass touch/mouse events directly to WebView for full mouse cursor support
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        showControlsTemporarily(force = false)
        return webView?.onTouchEvent(event ?: return false) ?: super.onTouchEvent(event)
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if ((event.source and InputDevice.SOURCE_CLASS_POINTER) != 0) {
            if (event.action == MotionEvent.ACTION_HOVER_MOVE || event.action == MotionEvent.ACTION_SCROLL) {
                showControlsTemporarily(force = false)
            }
            val handled = webView?.onGenericMotionEvent(event) ?: false
            if (handled) return true
        }
        return super.onGenericMotionEvent(event)
    }

    // dispatchKeyEvent intercepts ALL keys BEFORE the WebView gets them.
    // onKeyDown fires too late — WebView already consumed D-pad events by then.
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            showControlsTemporarily()
            when (event.keyCode) {
                KeyEvent.KEYCODE_BACK               -> { finish(); return true }
                // Media remote dedicated buttons
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE   -> { controlVideo("play_pause"); return true }
                KeyEvent.KEYCODE_MEDIA_PLAY         -> { controlVideo("play"); return true }
                KeyEvent.KEYCODE_MEDIA_PAUSE        -> { controlVideo("pause"); return true }
                KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> { controlVideo("fast_forward"); return true }
                KeyEvent.KEYCODE_MEDIA_REWIND       -> { controlVideo("rewind"); return true }
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                    if (controlsOverlay?.visibility == View.VISIBLE) {
                        currentFocus?.performClick()
                        return true
                    } else {
                        controlVideo("play_pause")
                        return true
                    }
                }
                KeyEvent.KEYCODE_DPAD_LEFT          -> {
                    controlVideo("rewind")
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT         -> {
                    controlVideo("fast_forward")
                    return true
                }
                KeyEvent.KEYCODE_DPAD_UP            -> {
                    controlVideo("vol_up")
                    return true
                }
                KeyEvent.KEYCODE_DPAD_DOWN          -> {
                    controlVideo("vol_down")
                    return true
                }
                // Channel/Page buttons = seek ±30s
                KeyEvent.KEYCODE_CHANNEL_UP,
                KeyEvent.KEYCODE_PAGE_UP            -> { controlVideo("seek_fwd_30"); return true }
                KeyEvent.KEYCODE_CHANNEL_DOWN,
                KeyEvent.KEYCODE_PAGE_DOWN          -> { controlVideo("seek_bck_30"); return true }
                KeyEvent.KEYCODE_MEDIA_NEXT         -> { playNextEpisode(); return true }
                KeyEvent.KEYCODE_MENU,
                KeyEvent.KEYCODE_SETTINGS           -> { tryNextProvider(); return true }
            }
        }
        // For everything else, let the WebView handle it normally
        return super.dispatchKeyEvent(event)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            webView?.requestFocus()
        }
    }

    override fun onUserLeaveHint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val params = android.app.PictureInPictureParams.Builder()
                    .setAspectRatio(android.util.Rational(16, 9))
                    .build()
                enterPictureInPictureMode(params)
            } catch (e: Exception) {
                android.util.Log.e("PlayerActivity", "Failed to enter Picture-in-Picture mode", e)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: android.content.res.Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            controlsOverlay?.visibility = View.GONE
        } else {
            controlsOverlay?.visibility = View.VISIBLE
        }
    }


    override fun onPause() {
        super.onPause()
        val player = exoPlayer
        if (player != null && !usingWebFallback) {
            player.pause()
            saveCurrentPosition(player.currentPosition, player.duration)
        } else if (usingWebFallback && webView != null) {
            webView?.onPause()
            webView?.pauseTimers()
        }
    }

    override fun onResume() {
        super.onResume()
        exoPlayer?.playWhenReady = true
        webView?.onResume()
        webView?.resumeTimers()
    }

    override fun onDestroy() {
        try {
            activityScope.cancel()
            uiHandler.removeCallbacksAndMessages(null)
            uiHandler.removeCallbacks(progressTicker)
            cancelScheduledCleanups()
            exoPlayer?.stop()
            exoPlayer?.release()
            exoPlayer = null
            nativePlayerView?.player = null
            nativePlayerView = null
            
            webView?.let { wv ->
                (wv.parent as? ViewGroup)?.removeView(wv)
                wv.stopLoading()
                wv.destroy()
            }
            
            if (::subtitleManager.isInitialized) {
                subtitleManager.clearCache()
            }
        } catch (t: Throwable) {
            android.util.Log.e("PlayerActivity", "Error in onDestroy cleanup", t)
        }
        webView = null
        super.onDestroy()
    }
}
