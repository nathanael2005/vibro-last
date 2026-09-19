# GEMINI.md - OnStream Android TV & Mobile Development Memory

## 1. Project Architecture & Known Environments
- **Device Tested:** Tecno LE6 (Android 11 / MediaTek PowerVR Rogue GE8320 / aarch64) and Android TV (1GB RAM Leanback).
- **Core Modules:** `:core` (domain models, API, logger), `:tv` (`com.nate.tv`, Compose for TV / Leanback), `:mobile` (`com.nate.mobile`).
- **Player & Sniffer:** `WebViewStreamExtractor.kt` runs a headless WebView that intercepts media stream requests (.m3u8, .mp4) and subtitle tracks (.vtt, .srt) from third-party embed providers.

---

## 2. Critical Bugs Solved & Lessons Learned

### A. The Silent Crash / Launcher Ejection Bug
- **Root Cause:**
  1. Previously, a new `WebView` was created and destroyed on every `loadMedia()` call. On low-memory devices (1GB TV), disposing Chromium asynchronously while immediately allocating another caused the Android Low Memory Killer (LMK) or native `libwebviewchromium.so` to crash the process silently.
  2. When a provider returned HTTP 403 (e.g. VidEasy), both `onReceivedHttpError` and `onReceivedError` fired simultaneously, causing duplicate calls to `advanceToNextCandidate()`. This triggered a race condition inside `loadUrl()` right when VidLink loaded, leading to SIGSEGV.
- **Rule to Never Violate:**
  - **Always reuse the singleton `sharedWebView`** via `resetWebViewToIdle()`. Never call `destroy()` during media switching.
  - **Always protect candidate transitions** with an `AtomicBoolean` guard (`isAdvancing.compareAndSet(false, true)`) and debouncing.

### B. Live Device Analysis (Tecno LE6 - 2026-09-19)
- **Logcat Evidence:**
  - App launched cleanly (PID: 17511).
  - Transition through all 6 candidates ran without crashing:
    1. **VidSrc (`vidsrc.me` -> `vidsrc.sh`):** Fails SSL validation (`CertPathValidatorException`) and times out.
    2. **MultiEmbed (`multiembed.mov`):** Cloudflare Turnstile CAPTCHA intercepts headless request.
    3. **VidEasy (`player.videasy.net`):** Returns HTTP 403 Forbidden.
    4. **VidLink (`vidlink.pro`):** Page loaded successfully without crashing, but stream was not served for the requested title (TMDB 287620).
    5. **2Embed (`2embed.cc`):** Timed out after 15s.
    6. **AutoEmbed (`autoembed.to`):** Redirected to ad domain (`filter.explorads.com`), timed out.
  - Overall status: Reached `[All extraction candidates exhausted]` gracefully without any SIGSEGV or process termination.

---

## 3. Streaming Provider Status & Troubleshooting
- For obscure or brand new titles (e.g. TMDB 287620), free embed scrapers typically have no cached streams, leading to timeouts on all providers.
- For mainstream titles (e.g. *The Mentalist* - TMDB 5920), VidSrc (`vidsrc.to`) and VidLink (`vidlink.pro`) have active, high-bitrate streams.

---

## 4. Stream Extraction Barriers & Fixes (2026-09-19)

### A. Root Causes of Failed Stream Extraction
1. **SSL Certificate Expiration on Old Domains:**
   - `vidsrc.me` redirects to `vidsrc.sh` with an invalid/mismatched SSL certificate, triggering `onReceivedError` in Chromium which prematurely advanced to the next candidate after 500ms.
   - **Fix:** Switched to working domains with valid SSL: `vidsrc.to`, `vidsrc.in`, `vidsrc.pm`.
2. **Cross-Origin Iframe Play Button Blockade:**
   - Modern embeds use deep iframe nesting: `vidsrc.to` -> `vsembed.ru` -> `cloudorchestranova.com`.
   - The landing page displays a `#bigPlay` button with `"autoStart": false`.
   - Because of the browser Same-Origin Policy, injected top-level Javascript cannot query or click DOM elements inside cross-origin child iframes!
   - **Fix 1 (HTML Rewriting):** In `shouldInterceptRequest`, intercept landing frame HTML requests and replace `"autoStart":false` / `"landing":true` with `"autoStart":true` / `"landing":false`. This causes the embed's own internal script to immediately inject the player iframe and start streaming without requiring any user click.
   - **Fix 2 (Native MotionEvent Dispatching):** Implemented `startPeriodicNativeTaps()` which dispatches native Android `MotionEvent` tap events at screen center `(w/2, h/2)`. Native Android input events penetrate all cross-origin iframe security boundaries at the Chromium window level.
3. **Software Rendering (`LAYER_TYPE_SOFTWARE`) vs Offscreen GPU Allocations:**
   - Disabling hardware acceleration previously disabled WebGL, hardware media decoders, and slowed down WebAssembly execution (needed by `vsdec.js` ChaCha20 decryption).
   - However, allocating a 1080p offscreen GPU buffer with `LAYER_TYPE_HARDWARE` crashed the low-end PowerVR Rogue GE8320 GPU driver (`ioctl c0044901 failed`).
   - **Fix:** Set layer type to `LAYER_TYPE_NONE` with a 720p layout size. This keeps standard hardware acceleration without wasting dedicated VRAM buffers on background WebViews.
4. **Outdated User-Agent:**
   - Upgraded `DESKTOP_USER_AGENT` to Chrome 124 (`Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ... Chrome/124.0.0.0 Safari/537.36`) to prevent anti-bot rejections.
5. **Top-Level Ad Navigation Hijacks:**
   - Ad scripts on embed pages often attempt to redirect the top-level window (`window.top.location`) to third-party ad networks (e.g. `newsboydurance.cfd`, `eng45.com`), destroying the player iframe before stream extraction finishes.
   - **Fix:** Implemented a strict whitelist check in `handleUrlOverride`: any navigation to a domain not matching `isAllowedStreamingDomain()` or a media/subtitle stream is immediately blocked (`return true`).

---

## 5. Android TV Remote Navigation & Focus Architecture (2026-09-19)

### A. Root Causes of TV Remote & Focus Freezes
1. **`onPreviewKeyEvent` Consuming D-Pad Navigation:**
   - In Compose, `onPreviewKeyEvent` is called on the container *before* key events reach focused children.
   - Previously, `onPreviewKeyEvent` consumed `KEYCODE_DPAD_UP`, `DPAD_DOWN`, `DPAD_LEFT`, `DPAD_RIGHT`, and `DPAD_CENTER` even when player controls were open, returning `true`.
   - As a result, the TV remote could never navigate between buttons (Rewind, Play/Pause, Forward, Close, Settings, Server, Subtitles, Audio, Quality, Episodes).
   - **Fix:** When `showControls == true`, `onPreviewKeyEvent` only handles dedicated hardware media keys (Play/Pause, Rewind, Fast Forward). Standard D-Pad keys are allowed to pass through to Compose's focus manager and the focused child controls. When controls are hidden, any D-Pad key wakes up the controls.
2. **`PlayerView` & `SurfaceView` Stealing Window Focus:**
   - Media3 `PlayerView` wraps a native Android `SurfaceView`.
   - If not explicitly blocked, the native Android view hierarchy gives focus to `PlayerView`/`SurfaceView`, swallowing all D-Pad input from the Compose tree.
   - **Fix:** Added `isFocusable = false`, `isFocusableInTouchMode = false`, and `descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS` to `PlayerView`, plus `.focusProperties { canFocus = false }` on the hosting `AndroidView`.
3. **Headless Scraper WebView Capturing D-Pad Events:**
   - The background WebView used for stream sniffing could take focus if focus was cleared from other views.
   - **Fix:** Added `wv.isFocusable = showWebInspector`, `wv.isFocusableInTouchMode = showWebInspector`, and `.focusProperties { canFocus = showWebInspector }` on its `AndroidView`.
4. **Deterministic 2D D-Pad Focus Traversal:**
   - Added explicit `focusProperties` wiring in `PlayerControls`:
     - From Play/Pause: UP goes to Close, LEFT to Rewind, RIGHT to Forward, DOWN to action bar.
     - From Close: DOWN goes to Play/Pause.
     - From Rewind / Forward: UP goes to Close, LEFT/RIGHT to Play/Pause.
     - From any Bottom Action Item: UP goes directly back to Play/Pause.
5. **Low-Memory Buffer Sizing for 1GB RAM Android TV:**
   - Default ExoPlayer buffered 120 seconds (up to 150MB VRAM/RAM), risking LMK kills on 1GB Android TV sticks.
   - Tuned `DefaultLoadControl` to 15s min / 45s max buffer with `setPrioritizeTimeOverSizeThresholds(true)` and 10s back buffer.

