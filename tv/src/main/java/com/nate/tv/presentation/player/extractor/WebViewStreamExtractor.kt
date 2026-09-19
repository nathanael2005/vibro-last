package com.nate.tv.presentation.player.extractor

import android.annotation.SuppressLint
import android.content.Context
import android.net.http.SslError
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.nate.core.common.AppLogger
import com.nate.core.domain.model.StreamLink
import com.nate.core.domain.model.SubtitleLink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.dnsoverhttps.DnsOverHttps
import java.io.ByteArrayInputStream
import java.net.InetAddress
import java.util.Collections
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Production-Hardened Headless Background Multi-Server Stream & Subtitle Interceptor.
 *
 * Runs invisibly on applicationContext in the background, executes client-side JavaScript,
 * drops ad/crypto-miner requests, bypasses SSL chain mismatches, detects and skips CAPTCHAs,
 * and intercepts raw HLS (.m3u8) / MP4 streams and sidecar (.vtt / .srt) subtitles with matching HTTP headers.
 */
class WebViewStreamExtractor(
    private val context: Context,
    private val onWebViewCreated: ((WebView?) -> Unit)? = null,
    private val onStreamExtracted: (StreamLink) -> Unit,
    private val onAllExtractionsFailed: () -> Unit,
    private val onStatusUpdate: (serverName: String, progressText: String) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val appContext = context.applicationContext
    private var webView: WebView? = null
    private val isExtracted = AtomicBoolean(false)
    private val isAdvancing = AtomicBoolean(false)
    private var currentAttemptIndex = 0
    private var embedSources: List<EmbedCandidate> = emptyList()
    private var timeoutRunnable: Runnable? = null
    private var nativeTapRunnable: Runnable? = null

    // Track intercepted sidecar subtitles during extraction
    private val interceptedSubtitles = Collections.synchronizedList(mutableListOf<SubtitleLink>())
    private val interceptedSubtitleUrls = Collections.synchronizedSet(mutableSetOf<String>())

    companion object {
        const val DESKTOP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

        @SuppressLint("StaticFieldLeak")
        private var sharedWebView: WebView? = null
    }

    data class EmbedCandidate(
        val serverId: String,
        val serverName: String,
        val embedUrl: String
    )

    fun startExtraction(
        tmdbId: String,
        mediaType: String,
        season: Int?,
        episode: Int?
    ) {
        val s = season ?: 1
        val ep = episode ?: 1
        val isTv = mediaType.equals("tv", ignoreCase = true) || mediaType.contains("show", ignoreCase = true) || season != null

        // Multi-Server Provider Pool (Prioritized by speed, stability, and live stream availability)
        embedSources = listOf(
            EmbedCandidate(
                serverId = "vidsrc_to",
                serverName = "VidSrc Cinema",
                embedUrl = if (isTv) "https://vidsrc.to/embed/tv/$tmdbId/$s/$ep" else "https://vidsrc.to/embed/movie/$tmdbId"
            ),
            EmbedCandidate(
                serverId = "vidsrc_in",
                serverName = "VidSrc Pro",
                embedUrl = if (isTv) "https://vidsrc.in/embed/tv/$tmdbId/$s/$ep" else "https://vidsrc.in/embed/movie/$tmdbId"
            ),
            EmbedCandidate(
                serverId = "vidsrc_pm",
                serverName = "VidSrc Prime",
                embedUrl = if (isTv) "https://vidsrc.pm/embed/tv/$tmdbId/$s/$ep" else "https://vidsrc.pm/embed/movie/$tmdbId"
            ),
            EmbedCandidate(
                serverId = "videasy",
                serverName = "VidEasy Ultra HD",
                embedUrl = if (isTv) "https://player.videasy.net/tv/$tmdbId/$s/$ep?overlay=true" else "https://player.videasy.net/movie/$tmdbId?overlay=true"
            ),
            EmbedCandidate(
                serverId = "vidlink_pro",
                serverName = "VidLink 1080p Ultra",
                embedUrl = if (isTv) "https://vidlink.pro/tv/$tmdbId/$s/$ep" else "https://vidlink.pro/movie/$tmdbId"
            ),
            EmbedCandidate(
                serverId = "2embed_cc",
                serverName = "2Embed HD Cinema",
                embedUrl = if (isTv) "https://www.2embed.cc/embedtv/$tmdbId?s=$s&e=$ep" else "https://www.2embed.cc/embed/$tmdbId"
            ),
            EmbedCandidate(
                serverId = "autoembed_to",
                serverName = "AutoEmbed Mirror",
                embedUrl = if (isTv) "https://autoembed.to/tv/tmdb/$tmdbId/$s/$ep" else "https://autoembed.to/movie/tmdb/$tmdbId"
            )
        )

        isExtracted.set(false)
        isAdvancing.set(false)
        currentAttemptIndex = 0
        interceptedSubtitles.clear()
        interceptedSubtitleUrls.clear()

        mainHandler.post {
            initHeadlessWebView()
            tryNextCandidate()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initHeadlessWebView() {
        try {
            var wv = sharedWebView
            if (wv == null) {
                val targetContext = try {
                    context.applicationContext
                } catch (_: Exception) {
                    context
                }
                wv = try {
                    WebView(targetContext)
                } catch (_: Exception) {
                    WebView(context)
                }

                wv!!.apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(1280, 720)
                    layout(0, 0, 1280, 720)

                    // 1. Standard window hardware acceleration without heavy offscreen GPU layer (prevents PowerVR GPU crash)
                    setLayerType(android.view.View.LAYER_TYPE_NONE, null)

                    // 2. Attach WebChromeClient (Required for TV JS execution & media elements)
                    webChromeClient = object : android.webkit.WebChromeClient() {
                        override fun onCreateWindow(
                            view: WebView?,
                            isDialog: Boolean,
                            isUserGesture: Boolean,
                            resultMsg: android.os.Message?
                        ): Boolean {
                            AppLogger.stream("🛡️ Blocked popup window creation")
                            return false
                        }

                        override fun onCloseWindow(window: WebView?) {
                            AppLogger.stream("🛡️ Blocked window.close()")
                        }
                    }

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_WAIVED, true)
                    }

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        allowFileAccess = false
                        allowContentAccess = false
                        javaScriptCanOpenWindowsAutomatically = false
                        setSupportMultipleWindows(false)
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        userAgentString = DESKTOP_USER_AGENT
                        cacheMode = WebSettings.LOAD_NO_CACHE
                    }
                }
                sharedWebView = wv
            } else {
                // Prepare existing persistent WebView for next extraction
                wv.stopLoading()
                (wv.parent as? android.view.ViewGroup)?.removeView(wv)
                wv.clearHistory()
                wv.resumeTimers()
            }

            // wv is guaranteed non-null here (either just created above or reused from sharedWebView)
            val nonNullWv: WebView = wv!!

            nonNullWv.addJavascriptInterface(
                JsBridge(
                    onMediaDetected = { detectedUrl ->
                        handlePotentialStream(detectedUrl, emptyMap())
                    },
                    onSubtitleDetected = { subUrl, lang, label ->
                        captureSubtitle(subUrl, lang, label)
                    }
                ),
                "AndroidSniffer"
            )

            nonNullWv.webViewClient = createWebViewClient()

            this.webView = nonNullWv
            onWebViewCreated?.invoke(nonNullWv)

            // Force-Enable Third-Party Cookies
            try {
                android.webkit.CookieManager.getInstance().apply {
                    setAcceptCookie(true)
                    setAcceptThirdPartyCookies(nonNullWv, true)
                }
            } catch (e: Exception) {
                AppLogger.error("Failed to enable third-party cookies on WebView", e)
            }
        } catch (e: Exception) {
            AppLogger.error("Failed to initialize headless WebView", e)
        }
    }

    private fun createWebViewClient(): WebViewClient {
        return object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                handler?.proceed()
            }

            private fun handleUrlOverride(reqUrl: android.net.Uri?): Boolean {
                if (reqUrl == null) return false
                val urlStr = reqUrl.toString()
                val scheme = reqUrl.scheme?.lowercase() ?: ""

                if (scheme == "data") {
                    try {
                        if (urlStr.contains("video/mp4", ignoreCase = true) || urlStr.contains(".m3u8", ignoreCase = true) || urlStr.contains(".mp4", ignoreCase = true)) {
                            if (urlStr.contains("base64,")) {
                                val b64 = urlStr.substringAfter("base64,").substringBefore("\"").substringBefore("'")
                                val decoded = String(android.util.Base64.decode(b64, android.util.Base64.DEFAULT))
                                val matcher = java.util.regex.Pattern.compile("https?://[^\"'\\s]+\\.(?:mp4|m3u8|mpd)[^\"'\\s]*").matcher(decoded)
                                if (matcher.find()) {
                                    val streamUrl = matcher.group()
                                    handlePotentialStream(streamUrl, emptyMap())
                                }
                            }
                        }
                    } catch (_: Exception) {}
                    return false
                }

                if (scheme == "about" || scheme == "blob" || scheme == "javascript") {
                    return false
                }

                if (scheme != "http" && scheme != "https") {
                    AppLogger.stream("🛡️ Blocked external intent/app launch ($scheme): $reqUrl")
                    return true
                }

                if (isAdOrMiner(urlStr)) {
                    AppLogger.stream("🛡️ Blocked ad navigation redirect: $reqUrl")
                    return true
                }

                val host = reqUrl.host?.lowercase() ?: ""
                if (host.contains("play.google.com") || host.contains("market.") ||
                    host.contains("t.me") || host.contains("telegram.") || urlStr.endsWith(".apk")
                ) {
                    AppLogger.stream("🛡️ Blocked external store/app hijack: $reqUrl")
                    return true
                }

                // Strict Ad Hijack Guard: Never allow top frame navigation to untrusted third-party ad networks
                if (!isAllowedStreamingDomain(host) && !isMediaStream(urlStr) && !isSubtitleTrack(urlStr)) {
                    AppLogger.stream("🛡️ Blocked ad redirect/hijack navigation ($host): $reqUrl")
                    return true
                }

                return false
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return handleUrlOverride(request?.url)
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                url: String?
            ): Boolean {
                return handleUrlOverride(url?.let { android.net.Uri.parse(it) })
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url?.toString() ?: return null
                val lower = url.lowercase()

                if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
                    return super.shouldInterceptRequest(view, request)
                }

                if (isAdOrMiner(url)) {
                    return emptyResponse()
                }

                if (isSubtitleTrack(lower)) {
                    captureSubtitle(url)
                    return super.shouldInterceptRequest(view, request)
                }

                if (isMediaStream(url)) {
                    val requestHeaders = request.requestHeaders ?: emptyMap()
                    handlePotentialStream(url, requestHeaders)
                }

                // Rewrite embed/landing pages to force autoStart & autoplay without requiring a click
                if (lower.contains("cloudorchestranova.com") || lower.contains("landing") ||
                    lower.contains("/embed/tv/") || lower.contains("/embed/movie/")
                ) {
                    val accept = request.requestHeaders?.get("Accept")?.lowercase() ?: ""
                    if (request.isForMainFrame || accept.contains("text/html") || lower.contains(".php") || !lower.contains(".")) {
                        try {
                            val reqBuilder = Request.Builder().url(url)
                            request.requestHeaders?.forEach { (k, v) -> reqBuilder.addHeader(k, v) }
                            if (!reqBuilder.build().headers.names().contains("User-Agent")) {
                                reqBuilder.addHeader("User-Agent", DESKTOP_USER_AGENT)
                            }
                            val resp = httpClient.newCall(reqBuilder.build()).execute()
                            if (resp.isSuccessful) {
                                val rawHtml = resp.body?.string() ?: ""
                                if (rawHtml.contains("autoStart") || rawHtml.contains("playerUrl") || rawHtml.contains("bigPlay")) {
                                    val modifiedHtml = rawHtml
                                        .replace("\"autoStart\":false", "\"autoStart\":true")
                                        .replace("\"autoStart\": false", "\"autoStart\": true")
                                        .replace("autoStart:false", "autoStart:true")
                                        .replace("autoStart: false", "autoStart: true")
                                        .replace("\"autoNext\":false", "\"autoNext\":true")
                                        .replace("\"landing\":true", "\"landing\":false")
                                        .replace("\"landing\": true", "\"landing\": false")
                                    AppLogger.stream("🚀 Force-enabled autoStart in embed HTML: $url")
                                    return WebResourceResponse(
                                        "text/html",
                                        "UTF-8",
                                        200,
                                        "OK",
                                        mapOf("Access-Control-Allow-Origin" to "*"),
                                        ByteArrayInputStream(modifiedHtml.toByteArray(Charsets.UTF_8))
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            AppLogger.web("AutoStart rewrite fallback for $url: ${e.message}")
                        }
                    }
                }

                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                AppLogger.web("Page started loading: $url")
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true && !isExtracted.get()) {
                    val errDesc = error?.description ?: "Connection failure"
                    val errCode = error?.errorCode ?: 0
                    if (errCode == WebViewClient.ERROR_FAILED_SSL_HANDSHAKE) {
                        AppLogger.web("Ignored non-fatal SSL error for ${request.url}")
                        return
                    }
                    AppLogger.stream("⚠️ Main frame failed to load ($errCode: $errDesc) for ${request.url}. Advancing in 2.5s...")
                    mainHandler.postDelayed({
                        if (!isExtracted.get()) {
                            advanceToNextCandidate()
                        }
                    }, 2500)
                }
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                val code = errorResponse?.statusCode ?: 0
                val reason = errorResponse?.reasonPhrase ?: ""
                if (request?.isForMainFrame == true && !isExtracted.get()) {
                    AppLogger.web("⚠️ HTTP Error $code ($reason) for ${request.url}")
                    if (code in 400..599) {
                        AppLogger.stream("⚠️ Provider rejected main frame with HTTP $code ($reason). Advancing...")
                        mainHandler.postDelayed({
                            if (!isExtracted.get()) {
                                advanceToNextCandidate()
                            }
                        }, 500)
                    }
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                AppLogger.web("Page finished loading: $url")
                if (url != null && !url.startsWith("about:")) {
                    injectSnifferScript(view)
                }
            }

            override fun onRenderProcessGone(
                view: WebView?,
                detail: android.webkit.RenderProcessGoneDetail?
            ): Boolean {
                val didCrash = detail?.didCrash() == true
                AppLogger.error("🚨 WebView renderer process exited (didCrash=$didCrash). Host app protected.")
                try {
                    (view?.parent as? android.view.ViewGroup)?.removeView(view)
                } catch (_: Exception) {}
                onWebViewCreated?.invoke(null)
                webView = null
                sharedWebView = null
                mainHandler.post {
                    try {
                        view?.destroy()
                    } catch (_: Exception) {}
                    if (!isExtracted.get()) {
                        advanceToNextCandidate()
                    }
                }
                return true
            }
        }
    }

    private fun startPeriodicNativeTaps() {
        stopPeriodicNativeTaps()
        val runnable = object : Runnable {
            private var count = 0
            override fun run() {
                if (isExtracted.get() || count >= 8) return
                count++
                val wv = webView
                if (wv != null && wv.width > 0 && wv.height > 0) {
                    val cx = wv.width / 2f
                    val cy = wv.height / 2f
                    val now = SystemClock.uptimeMillis()
                    try {
                        val down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, cx, cy, 0)
                        val up = MotionEvent.obtain(now, now + 40, MotionEvent.ACTION_UP, cx, cy, 0)
                        wv.dispatchTouchEvent(down)
                        wv.dispatchTouchEvent(up)
                        down.recycle()
                        up.recycle()
                    } catch (_: Exception) {}
                }
                mainHandler.postDelayed(this, 1000)
            }
        }
        nativeTapRunnable = runnable
        mainHandler.postDelayed(runnable, 1200)
    }

    private fun stopPeriodicNativeTaps() {
        nativeTapRunnable?.let { mainHandler.removeCallbacks(it) }
        nativeTapRunnable = null
    }

    private fun tryNextCandidate() {
        if (isExtracted.get()) return

        if (currentAttemptIndex >= embedSources.size) {
            AppLogger.stream("All stream extraction candidates exhausted.")
            stopPeriodicNativeTaps()
            resetWebViewToIdle()
            onAllExtractionsFailed()
            return
        }

        val candidate = embedSources[currentAttemptIndex]
        onStatusUpdate(candidate.serverName, "Negotiating tokens & resolving native stream (${candidate.serverName})...")
        AppLogger.stream("Sniffing provider [${currentAttemptIndex + 1}/${embedSources.size}]: ${candidate.serverName} -> ${candidate.embedUrl}")

        try {
            webView?.loadUrl(candidate.embedUrl)
            startPeriodicNativeTaps()
        } catch (e: Exception) {
            AppLogger.error("Failed to load candidate URL in headless WebView", e)
            advanceToNextCandidate()
            return
        }

        timeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        timeoutRunnable = Runnable {
            if (!isExtracted.get()) {
                AppLogger.stream("Candidate ${candidate.serverName} timed out after 15s. Advancing...")
                advanceToNextCandidate()
            }
        }
        mainHandler.postDelayed(timeoutRunnable!!, 15000)
    }

    fun advanceToNextCandidate() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { advanceToNextCandidate() }
            return
        }
        if (isExtracted.get()) return
        if (isAdvancing.getAndSet(true)) {
            AppLogger.stream("⚠️ Already advancing to next candidate, ignoring duplicate trigger")
            return
        }
        stopPeriodicNativeTaps()
        timeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        interceptedSubtitles.clear()
        interceptedSubtitleUrls.clear()

        currentAttemptIndex++
        if (currentAttemptIndex >= embedSources.size) {
            AppLogger.stream("All stream extraction candidates exhausted.")
            isAdvancing.set(false)
            resetWebViewToIdle()
            onAllExtractionsFailed()
            return
        }

        val candidate = embedSources[currentAttemptIndex]
        AppLogger.player("⏭️ Advancing to candidate #${currentAttemptIndex + 1}/${embedSources.size}: ${candidate.serverName}")

        webView?.stopLoading()
        webView?.loadUrl("about:blank")
        webView?.clearHistory()

        mainHandler.postDelayed({
            isAdvancing.set(false)
            tryNextCandidate()
        }, 350)
    }

    fun skipCurrentCandidate() {
        advanceToNextCandidate()
    }

    private fun captureSubtitle(subtitleUrl: String, explicitLang: String? = null, explicitLabel: String? = null) {
        if (interceptedSubtitleUrls.add(subtitleUrl)) {
            val lower = subtitleUrl.lowercase()
            val format = if (lower.contains(".srt")) "SRT" else "VTT"
            
            val lang = explicitLang ?: when {
                lower.contains("/en") || lower.contains("_en") || lower.contains("eng") -> "en"
                lower.contains("/am") || lower.contains("_am") || lower.contains("amh") -> "am"
                lower.contains("/es") || lower.contains("_es") || lower.contains("spa") -> "es"
                lower.contains("/fr") || lower.contains("_fr") || lower.contains("fra") -> "fr"
                lower.contains("/ar") || lower.contains("_ar") || lower.contains("ara") -> "ar"
                lower.contains("/de") || lower.contains("_de") || lower.contains("ger") -> "de"
                lower.contains("/it") || lower.contains("_it") || lower.contains("ita") -> "it"
                lower.contains("/pt") || lower.contains("_pt") || lower.contains("por") -> "pt"
                lower.contains("/ru") || lower.contains("_ru") || lower.contains("rus") -> "ru"
                else -> "und"
            }
            val label = explicitLabel ?: when (lang) {
                "en" -> "English"
                "am" -> "Amharic"
                "es" -> "Spanish"
                "fr" -> "French"
                "ar" -> "Arabic"
                "de" -> "German"
                "it" -> "Italian"
                "pt" -> "Portuguese"
                "ru" -> "Russian"
                else -> "Subtitle (${interceptedSubtitles.size + 1})"
            }

            val subLink = SubtitleLink(lang = lang, label = label, url = subtitleUrl, format = format)
            interceptedSubtitles.add(subLink)
            AppLogger.stream("📑 Sniffed sidecar subtitle: $label -> $subtitleUrl")
        }
    }

    private val bootstrapClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()

    private val dohDns = try {
        DnsOverHttps.Builder()
            .client(bootstrapClient)
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

    private val httpClient = OkHttpClient.Builder()
        .apply { dohDns?.let { dns(it) } }
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private fun isRealStream(url: String, headers: Map<String, String>): Boolean {
        // 1. Instantly accept blob: URLs
        if (url.startsWith("blob:", ignoreCase = true)) {
            return true
        }

        // 2. Reject non-HTTP protocols (e.g. wss://, ws://, data:, file:) before OkHttp crashes
        if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
            return false
        }

        return try {
            val candidate = embedSources.getOrNull(currentAttemptIndex) ?: embedSources.first()
            val requestBuilder = Request.Builder().url(url)
            headers.forEach { (key, value) -> requestBuilder.addHeader(key, value) }

            if (!headers.containsKey("User-Agent") && !headers.containsKey("user-agent")) {
                requestBuilder.addHeader("User-Agent", DESKTOP_USER_AGENT)
            }
            if (!headers.containsKey("Referer") && !headers.containsKey("referer")) {
                if (url.contains("mooncase", ignoreCase = true) || url.contains("hakuna", ignoreCase = true)) {
                    requestBuilder.addHeader("Referer", "https://bcdn.hakunaymatata.com/")
                } else {
                    requestBuilder.addHeader("Referer", candidate.embedUrl)
                }
            }

            // For direct MP4 or other container files, test with byte-range request
            if (!url.contains(".m3u8", ignoreCase = true)) {
                requestBuilder.addHeader("Range", "bytes=0-1024")
                val response = httpClient.newCall(requestBuilder.build()).execute()
                val code = response.code
                response.close()
                if (code in 200..299) {
                    AppLogger.stream("🎯 Direct stream reachable ($code): $url")
                    return true
                } else if (code == 403 || code == 404 || code == 410) {
                    AppLogger.stream("⚠️ Direct stream rejected with HTTP $code: $url")
                    return false
                }
                return true
            }

            val response = httpClient.newCall(requestBuilder.build()).execute()

            // 3. Fallback: If OkHttp is blocked by Cloudflare (non-200), default to TRUE so the stream isn't lost
            if (!response.isSuccessful) {
                AppLogger.stream("⚠️ OkHttp received status ${response.code} for $url, defaulting to TRUE")
                return true
            }

            val manifest = response.body?.string() ?: return true

            // 4. Master playlists (contains variant qualities) -> Always real
            if (manifest.contains("#EXT-X-STREAM-INF") || manifest.contains("#EXT-X-MEDIA")) {
                AppLogger.stream("🎯 Master playlist verified: $url")
                return true
            }

            // 5. Single playlists: Filter out short pre-roll ads (< 15 segments)
            val segmentCount = manifest.split("#EXTINF").size - 1
            AppLogger.stream("📊 Manifest segment count for $url: $segmentCount")
            segmentCount >= 15 || segmentCount == 0
        } catch (e: Exception) {
            // Fallback to TRUE on network/parse errors to prevent dropping the real movie
            AppLogger.stream("⚠️ Manifest check error ($url): ${e.message}, defaulting to TRUE")
            true
        }
    }

    private fun handlePotentialStream(mediaUrl: String, headers: Map<String, String>) {
        if (isExtracted.get()) return

        // 1. Fetch all cookies on the MAIN thread before switching to IO
        mainHandler.post {
            if (isExtracted.get()) return@post

            val candidate = embedSources.getOrNull(currentAttemptIndex) ?: embedSources.first()
            val currentWebUrl = webView?.url ?: ""

            // Gather all active session cookies across current page, candidate host, and media host
            val activeCookie = try {
                val cm = android.webkit.CookieManager.getInstance()
                val c1 = cm.getCookie(currentWebUrl)
                val c2 = cm.getCookie(candidate.embedUrl)
                val c3 = cm.getCookie(mediaUrl)
                listOfNotNull(c1, c2, c3)
                    .flatMap { it.split(";") }
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .joinToString("; ")
            } catch (e: Exception) {
                ""
            }

            val updatedHeaders = headers.toMutableMap()
            if (activeCookie.isNotEmpty() && !updatedHeaders.containsKey("Cookie")) {
                updatedHeaders["Cookie"] = activeCookie
                AppLogger.stream("🍪 Attached active WebView session cookie (${activeCookie.length} chars)")
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (isRealStream(mediaUrl, updatedHeaders)) {
                        if (!isExtracted.getAndSet(true)) {
                            withContext(Dispatchers.Main) {
                                finalizeAndExtractStream(mediaUrl, updatedHeaders)
                            }
                        }
                    } else {
                        AppLogger.stream("🚫 Ignored short ad stream: $mediaUrl")
                    }
                } catch (e: Exception) {
                    AppLogger.error("OkHttp validation failed for $mediaUrl: ${e.message}")
                }
            }
        }
    }

    private fun finalizeAndExtractStream(mediaUrl: String, headers: Map<String, String>) {
        stopPeriodicNativeTaps()
        timeoutRunnable?.let { mainHandler.removeCallbacks(it) }

        val candidate = embedSources.getOrNull(currentAttemptIndex) ?: embedSources.first()
        val format = when {
            mediaUrl.contains(".mpd", ignoreCase = true) || mediaUrl.contains("manifest.mpd", ignoreCase = true) -> "DASH"
            mediaUrl.contains(".m3u8", ignoreCase = true) || mediaUrl.contains("/hls/", ignoreCase = true) -> "HLS"
            else -> "MP4"
        }

        // Build exact matching HTTP headers for ExoPlayer
        val requestHeaders = headers.toMutableMap()

        if (!requestHeaders.containsKey("User-Agent") && !requestHeaders.containsKey("user-agent")) {
            requestHeaders["User-Agent"] = DESKTOP_USER_AGENT
        }

        var explicitReferer: String? = null
        var cleanUrl = mediaUrl
        try {
            val uri = android.net.Uri.parse(mediaUrl)
            val hParam = uri.getQueryParameter("headers") ?: uri.getQueryParameter("referer")
            if (hParam != null) {
                // Strip the headers query parameter from the stream URL
                cleanUrl = mediaUrl
                    .replace(Regex("[&?]headers=[^&]*"), "")
                    .replace(Regex("[&?]referer=[^&]*"), "")
                    .replace("?&", "?")
                if (cleanUrl.endsWith("?")) {
                    cleanUrl = cleanUrl.substringBeforeLast("?")
                }

                val decodedHParam = try {
                    java.net.URLDecoder.decode(hParam, "UTF-8")
                } catch (_: Exception) {
                    hParam
                }

                if (decodedHParam.trim().startsWith("{")) {
                    try {
                        val json = org.json.JSONObject(decodedHParam)
                        val keys = json.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val v = json.optString(k)
                            if (v.isNotBlank()) {
                                requestHeaders[k] = v
                                if (k.equals("Referer", ignoreCase = true)) {
                                    explicitReferer = v
                                } else if (k.equals("Origin", ignoreCase = true)) {
                                    requestHeaders["Origin"] = v
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }

                if (explicitReferer == null) {
                    if (decodedHParam.contains("hakunaymatata.com", ignoreCase = true) || decodedHParam.contains("hakunamatata.com", ignoreCase = true)) {
                        explicitReferer = "https://bcdn.hakunaymatata.com/"
                        requestHeaders["Origin"] = "https://bcdn.hakunaymatata.com"
                    } else if (decodedHParam.startsWith("http", ignoreCase = true)) {
                        explicitReferer = decodedHParam
                    } else if (decodedHParam.contains("Referer:", ignoreCase = true)) {
                        explicitReferer = decodedHParam.substringAfter("Referer:").trim()
                    }
                }
            }
        } catch (_: Exception) {}

        // Fallback for CDN domains that require HakunaMatata referers
        if (explicitReferer == null && (cleanUrl.contains("mooncase", ignoreCase = true) || cleanUrl.contains("hakuna", ignoreCase = true))) {
            explicitReferer = "https://bcdn.hakunaymatata.com/"
            requestHeaders["Origin"] = "https://bcdn.hakunaymatata.com"
        }

        val currentWebUrl = webView?.url?.takeIf { it.isNotBlank() }
        val finalReferer = explicitReferer ?: currentWebUrl ?: candidate.embedUrl
        val originHost = android.net.Uri.parse(finalReferer).let { "${it.scheme ?: "https"}://${it.host ?: ""}" }

        if (!requestHeaders.containsKey("Referer") && !requestHeaders.containsKey("referer")) {
            requestHeaders["Referer"] = finalReferer
        }
        if (!requestHeaders.containsKey("Origin") && !requestHeaders.containsKey("origin")) {
            requestHeaders["Origin"] = originHost
        }

        // 800ms buffer to allow JSON payloads, DOM tracks, and network subtitles to register
        mainHandler.postDelayed({
            val streamLink = StreamLink(
                serverId = candidate.serverId,
                serverName = "⚡ Native Stream (${candidate.serverName})",
                url = cleanUrl,
                format = format,
                headers = requestHeaders,
                subtitles = ArrayList(interceptedSubtitles)
            )

            AppLogger.stream("✅ Native Stream Captured ($format): ${streamLink.url} with ${streamLink.subtitles.size} sidecar subtitles")

            resetWebViewToIdle()
            onStreamExtracted(streamLink)
        }, 800)
    }

    private fun isMediaStream(url: String): Boolean {
        val lower = url.lowercase()

        // Exclude static assets
        if (lower.endsWith(".js") || lower.endsWith(".css") || lower.endsWith(".png") ||
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".svg") ||
            lower.endsWith(".ico") || lower.endsWith(".woff") || lower.endsWith(".ttf") ||
            lower.endsWith(".vtt") || lower.endsWith(".srt")
        ) {
            return false
        }

        // Exclude ad video streams
        if (isAdOrMiner(url)) {
            return false
        }

        return lower.contains(".m3u8") ||
                lower.contains(".mpd") ||
                lower.contains(".mp4") ||
                lower.contains("/hls/") ||
                lower.contains("master.m3u8") ||
                lower.contains("index.m3u8") ||
                lower.contains("playlist.m3u8") ||
                lower.contains("manifest.mpd") ||
                lower.contains("chunklist") ||
                lower.contains("tracks-v") ||
                (lower.contains("stream") && (lower.contains(".m3u8") || lower.contains(".mpd")))
    }

    private fun isSubtitleTrack(lower: String): Boolean {
        return lower.endsWith(".vtt") || lower.endsWith(".srt") ||
                lower.contains("/subtitles/") || lower.contains("/captions/") ||
                (lower.contains("sub") && (lower.endsWith(".vtt") || lower.endsWith(".srt")))
    }

    private fun isAllowedStreamingDomain(host: String): Boolean {
        val lower = host.lowercase()
        return lower.contains("videasy") || lower.contains("vidlink") ||
                lower.contains("vidsrc") || lower.contains("2embed") ||
                lower.contains("multiembed") || lower.contains("streamingnow") ||
                lower.contains("vsembed") || lower.contains("cloudorchestranova") ||
                lower.contains("autoembed") || lower.contains("jongleurjamboree") ||
                lower.contains("hakunaymatata") || lower.contains("bunnycdn") || lower.contains("cloudfront") ||
                lower.contains("fastly") || lower.contains("mcloud") ||
                lower.contains("streamtape") || lower.contains("dood") ||
                lower.contains("upstream") || lower.contains("videomega")
    }

    private fun isAdOrMiner(url: String): Boolean {
        val uri = try { android.net.Uri.parse(url) } catch (_: Exception) { null }
        val host = uri?.host?.lowercase() ?: ""
        val path = uri?.path?.lowercase() ?: url.lowercase()

        // 1. Exact / Suffix Domain Blocklist (Ad Networks, Trackers, Popunders)
        val adDomains = listOf(
            "doubleclick.net", "googleads", "googlesyndication.com", "googletagservices.com",
            "adservice.google.com", "popads.net", "popcash.net", "adsterra.com", "exoclick.com",
            "adcash.com", "propellerads.com", "onclickads.net", "monetag.com", "hilltopads.com",
            "richpush.co", "a-ads.com", "coinhive.com", "crypto-loot.com", "innovid.com",
            "spotxchange.com", "springserve.com", "freewheel.tv", "aniview.com", "outbrain.com",
            "taboola.com", "mgid.com", "revcontent.com", "adnxs.com", "rubiconproject.com",
            "pubmatic.com", "openx.net", "criteo.com", "bidswitch.net", "casalemedia.com",
            "histats.com", "statcounter.com", "hotjar.com", "crazyegg.com", "clarity.ms",
            "google-analytics.com", "segment.io", "mixpanel.com", "whos.amung.us", "tagivi.com",
            "bet365.com", "1xbet.com", "melbet.com", "mostbet.com", "parimatch.com", "betway.com",
            "trafficjunky.com", "juicyads.com", "trafficfactory.biz", "adbuffs.com",
            "explorads.com", "pushhub.net", "adspredictiv.com", "inadsexchange.com"
        )

        for (domain in adDomains) {
            if (host.contains(domain) || host.endsWith(domain)) {
                return true
            }
        }

        // 2. Strict Path & Signature Boundaries (Preserves movie titles like Casino Royale or Devastating)
        if (path.contains("/pagead/") || path.contains("/vast/") || path.contains("/vpaid/") ||
            path.contains("/preroll/") || path.contains("/midroll/") || path.contains("/popunder/") ||
            path.contains("/ads/") || path.contains("/adsystem/") || path.contains("/adserver/") ||
            path.contains("disable-devtool") || path.contains("devtools-detector")
        ) {
            return true
        }

        return false
    }

    private fun emptyResponse(): WebResourceResponse {
        val response = WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0)))
        // Required for older Android TV Chromium versions to prevent native SIGSEGV
        response.setStatusCodeAndReasonPhrase(200, "OK")
        response.responseHeaders = mapOf(
            "Access-Control-Allow-Origin" to "*",
            "Cache-Control" to "no-cache",
            "Content-Length" to "0"
        )
        return response
    }

    private fun injectSnifferScript(view: WebView?) {
        val js = """
            (function() {
                try {
                    // 0. Neutralize Popups, Window Hijacks & Modal Dialogs
                    try {
                        window.open = function() { return null; };
                        window.alert = function() {};
                        window.confirm = function() { return true; };
                        window.prompt = function() { return null; };
                    } catch(e) {}

                    // Helper to recursively extract subtitles from JSON/JS objects
                    function extractSubtitlesFromObj(obj, depth) {
                        try {
                            if (!obj || (depth || 0) > 4) return;
                            var nextDepth = (depth || 0) + 1;
                            if (Array.isArray(obj)) {
                                obj.forEach(function(item) { extractSubtitlesFromObj(item, nextDepth); });
                                return;
                            }
                            if (typeof obj === 'object') {
                                var url = obj.file || obj.url || obj.src || obj.link;
                                var label = obj.label || obj.name || obj.language || obj.lang || 'English';
                                var lang = obj.language || obj.lang || obj.srclang || 'en';
                                var kind = (obj.kind || obj.type || '').toLowerCase();
                                if (url && typeof url === 'string' && (url.indexOf('.vtt') !== -1 || url.indexOf('.srt') !== -1 || kind.indexOf('sub') !== -1 || kind.indexOf('cap') !== -1 || obj.label)) {
                                    if (window.AndroidSniffer && window.AndroidSniffer.onSubtitleDetected) {
                                        window.AndroidSniffer.onSubtitleDetected(url, lang, label);
                                    }
                                }
                                if (obj.tracks) extractSubtitlesFromObj(obj.tracks, nextDepth);
                                if (obj.subtitles) extractSubtitlesFromObj(obj.subtitles, nextDepth);
                                if (obj.captions) extractSubtitlesFromObj(obj.captions, nextDepth);
                            }
                        } catch(e) {}
                    }

                    // 1. Hook JSON.parse for player subtitle configurations
                    var origJSONParse = JSON.parse;
                    JSON.parse = function() {
                        var res = origJSONParse.apply(this, arguments);
                        try { extractSubtitlesFromObj(res); } catch(e) {}
                        return res;
                    };

                    // 2. Hook XMLHttpRequest
                    var origOpen = XMLHttpRequest.prototype.open;
                    XMLHttpRequest.prototype.open = function(method, url) {
                        if (url && typeof url === 'string') {
                            var l = url.toLowerCase();
                            if (l.indexOf('.m3u8') !== -1 || l.indexOf('.mpd') !== -1 || l.indexOf('.mp4') !== -1 || l.indexOf('/hls/') !== -1 || l.indexOf('master') !== -1 || l.indexOf('manifest') !== -1) {
                                if (window.AndroidSniffer) { window.AndroidSniffer.onMediaDetected(url); }
                            }
                            if (l.indexOf('.vtt') !== -1 || l.indexOf('.srt') !== -1 || l.indexOf('/subtitles/') !== -1) {
                                if (window.AndroidSniffer && window.AndroidSniffer.onSubtitleDetected) {
                                    window.AndroidSniffer.onSubtitleDetected(url, 'en', 'English');
                                }
                            }
                        }
                        return origOpen.apply(this, arguments);
                    };

                    // 3. Hook Fetch API
                    if (window.fetch) {
                        var origFetch = window.fetch;
                        window.fetch = function() {
                            var url = arguments[0];
                            if (typeof url === 'string') {
                                var l = url.toLowerCase();
                                if (l.indexOf('.m3u8') !== -1 || l.indexOf('.mpd') !== -1 || l.indexOf('.mp4') !== -1 || l.indexOf('/hls/') !== -1 || l.indexOf('master') !== -1 || l.indexOf('manifest') !== -1) {
                                    if (window.AndroidSniffer) { window.AndroidSniffer.onMediaDetected(url); }
                                }
                                if (l.indexOf('.vtt') !== -1 || l.indexOf('.srt') !== -1 || l.indexOf('/subtitles/') !== -1) {
                                    if (window.AndroidSniffer && window.AndroidSniffer.onSubtitleDetected) {
                                        window.AndroidSniffer.onSubtitleDetected(url, 'en', 'English');
                                    }
                                }
                            }
                            return origFetch.apply(this, arguments);
                        };
                    }

                    // 4. Hook HTMLMediaElement src
                    var originalSrc = Object.getOwnPropertyDescriptor(HTMLMediaElement.prototype, 'src');
                    if (originalSrc && originalSrc.set) {
                        Object.defineProperty(HTMLMediaElement.prototype, 'src', {
                            set: function(val) {
                                if (val && typeof val === 'string') {
                                    var l = val.toLowerCase();
                                    if (l.indexOf('.m3u8') !== -1 || l.indexOf('.mpd') !== -1 || l.indexOf('.mp4') !== -1 || l.indexOf('/hls/') !== -1 || l.indexOf('master') !== -1 || l.indexOf('manifest') !== -1) {
                                        if (window.AndroidSniffer) { window.AndroidSniffer.onMediaDetected(val); }
                                    }
                                }
                                return originalSrc.set.apply(this, arguments);
                            }
                        });
                    }

                    // 4b. Hook window.postMessage & message events (VidSrc / streamge.org)
                    var handleMsgPayload = function(data) {
                        if (!data) return;
                        if (typeof data === 'string') {
                            try {
                                if (data.indexOf('{') !== -1) {
                                    var parsed = JSON.parse(data);
                                    data = parsed;
                                }
                            } catch(e) {}
                        }
                        if (typeof data === 'object') {
                            var u = data.url || data.file || data.src || data.link || data.stream || data.source;
                            if (u && typeof u === 'string') {
                                var l = u.toLowerCase();
                                if (l.indexOf('.m3u8') !== -1 || l.indexOf('.mpd') !== -1 || l.indexOf('.mp4') !== -1 || l.indexOf('/hls/') !== -1) {
                                    if (window.AndroidSniffer) { window.AndroidSniffer.onMediaDetected(u); }
                                }
                            }
                        } else if (typeof data === 'string' && data.startsWith('http')) {
                            var l = data.toLowerCase();
                            if (l.indexOf('.m3u8') !== -1 || l.indexOf('.mpd') !== -1 || l.indexOf('.mp4') !== -1 || l.indexOf('/hls/') !== -1) {
                                if (window.AndroidSniffer) { window.AndroidSniffer.onMediaDetected(data); }
                            }
                        }
                    };

                    window.addEventListener('message', function(ev) {
                        try { handleMsgPayload(ev.data); } catch(e) {}
                    });

                    var origPostMessage = window.postMessage;
                    window.postMessage = function(msg, targetOrigin, transfer) {
                        try { handleMsgPayload(msg); } catch(e) {}
                        return origPostMessage.apply(this, arguments);
                    };

                    // 5. Track Element Scanner
                    var scanTracks = function() {
                        try {
                            var tracks = document.querySelectorAll('track');
                            for (var i = 0; i < tracks.length; i++) {
                                var t = tracks[i];
                                if (t.src && window.AndroidSniffer && window.AndroidSniffer.onSubtitleDetected) {
                                    window.AndroidSniffer.onSubtitleDetected(t.src, t.srclang || 'en', t.label || t.srclang || 'English');
                                }
                            }
                        } catch(e) {}
                    };
                    scanTracks();
                    var trackInterval = setInterval(scanTracks, 1500);
                    setTimeout(function() { clearInterval(trackInterval); }, 15000);

                    // 6. Synthetic User-Interaction, Ad Fast-Forward & Autoplay Dispatcher
                    var triggerPlayAndSkipAds = function() {
                        try {
                            // Neutralize target="_blank" so links cannot escape the app to an external browser
                            var blankLinks = document.querySelectorAll('a[target="_blank"]');
                            for (var j = 0; j < blankLinks.length; j++) {
                                blankLinks[j].removeAttribute('target');
                                blankLinks[j].setAttribute('target', '_self');
                            }

                            // Fast-forward video ads (< 120s) to 16x speed and mute them
                            var vids = document.getElementsByTagName('video');
                            for (var i = 0; i < vids.length; i++) {
                                var v = vids[i];
                                v.muted = true;
                                if (v.duration && v.duration > 0 && v.duration < 120) {
                                    v.playbackRate = 16.0;
                                }
                                if (v.src && (v.src.indexOf('.m3u8') !== -1 || v.src.indexOf('.mpd') !== -1 || v.src.indexOf('.mp4') !== -1)) {
                                    if (window.AndroidSniffer) { window.AndroidSniffer.onMediaDetected(v.src); }
                                }
                                v.play().catch(function(){});
                            }

                            // Auto-click skip-ad buttons
                            var skipBtns = document.querySelectorAll('.skip-ad, .jw-skip, .vjs-skip-ad, .video-ad-skip, [class*="skip" i], [aria-label*="skip" i]');
                            skipBtns.forEach(function(b) { try { b.click(); } catch(e) {} });

                            // Dispatch synthetic click & pointerdown ONLY to genuine video play elements
                            var playElements = document.querySelectorAll(
                                '.play-btn, .jw-display-icon-container, .vjs-big-play-button, ' +
                                '[aria-label*="Play" i], [title*="Play" i], ' +
                                'button.play, button[class*="play" i], .play-icon, .player-poster'
                            );
                            playElements.forEach(function(el) {
                                try {
                                    el.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
                                    el.dispatchEvent(new Event('pointerdown', { bubbles: true, cancelable: true }));
                                } catch(err) {}
                            });
                        } catch(e) {}
                    };
                    triggerPlayAndSkipAds();
                    var playInterval = setInterval(triggerPlayAndSkipAds, 250);
                    setTimeout(function() { clearInterval(playInterval); }, 8000);

                } catch(e) {}
            })();
        """.trimIndent()

        view?.evaluateJavascript(js, null)
    }

    fun pause() {
        mainHandler.post {
            try {
                webView?.pauseTimers()
                webView?.onPause()
            } catch (_: Exception) {}
        }
    }

    fun resume() {
        mainHandler.post {
            try {
                webView?.onResume()
                webView?.resumeTimers()
            } catch (_: Exception) {}
        }
    }

    fun release() {
        timeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        mainHandler.post {
            resetWebViewToIdle()
        }
    }

    private fun resetWebViewToIdle() {
        try {
            stopPeriodicNativeTaps()
            timeoutRunnable?.let { mainHandler.removeCallbacks(it) }
            val wv = webView
            webView = null
            onWebViewCreated?.invoke(null)
            wv?.apply {
                stopLoading()
                loadUrl("about:blank")
                clearHistory()
                clearCache(true)
                (parent as? android.view.ViewGroup)?.removeView(this)
                pauseTimers()
            }
        } catch (_: Exception) {}
    }

    private class JsBridge(
        private val onMediaDetected: (String) -> Unit,
        private val onSubtitleDetected: (String, String?, String?) -> Unit
    ) {
        @JavascriptInterface
        fun onMediaDetected(url: String) {
            if (url.isNotBlank() && url.startsWith("http")) {
                onMediaDetected.invoke(url)
            }
        }

        @JavascriptInterface
        fun onSubtitleDetected(url: String, lang: String?, label: String?) {
            if (url.isNotBlank() && url.startsWith("http")) {
                onSubtitleDetected.invoke(url, lang, label)
            }
        }
    }
}
