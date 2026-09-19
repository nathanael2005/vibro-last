package com.nate.app.util

import android.util.Log
import androidx.media3.common.C
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.ExoTrackSelection
import androidx.media3.exoplayer.upstream.DefaultAllocator
import androidx.media3.exoplayer.upstream.BandwidthMeter

class AdaptiveLoadControl(
    private val bandwidthMeter: BandwidthMeter,
    private val minBufferMs: Int = 15_000,
    private val maxBufferMs: Int = 50_000,
    private val targetBufferMs: Int = 30_000,
    private val bufferForPlaybackMs: Int = 2_500,
    private val bufferForPlaybackAfterRebufferMs: Int = 5_000,
    private val requiredBitrateBps: Long = 5_000_000L // 5 Mbps baseline for 1080p
) : LoadControl {

    companion object {
        private const val TAG = "AdaptiveLoadControl"
    }

    private val allocator = DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE)
    private var targetBufferSize = 0
    private var isBuffering = false

    override fun onPrepared() {
        reset(false)
    }

    override fun onStopped() {
        reset(true)
    }

    override fun onReleased() {
        reset(true)
    }

    override fun getAllocator(): DefaultAllocator {
        return allocator
    }

    override fun getBackBufferDurationUs(): Long {
        return 0L
    }

    override fun retainBackBufferFromKeyframe(): Boolean {
        return false
    }

    override fun onTracksSelected(
        renderers: Array<out Renderer>,
        trackGroups: TrackGroupArray,
        trackSelections: Array<out ExoTrackSelection>
    ) {
        var targetSize = 0
        for (i in renderers.indices) {
            if (trackSelections[i] != null) {
                targetSize += when (renderers[i].trackType) {
                    C.TRACK_TYPE_VIDEO -> 12 * 1024 * 1024 // 12MB video allocation
                    C.TRACK_TYPE_AUDIO -> 2 * 1024 * 1024  // 2MB audio allocation
                    else -> 1024 * 1024                    // 1MB other
                }
            }
        }
        targetBufferSize = targetSize.coerceAtLeast(6 * 1024 * 1024)
        allocator.setTargetBufferSize(targetBufferSize)
        Log.d(TAG, "Tracks selected. Configured target allocator buffer size: $targetBufferSize bytes")
    }

    /**
     * Implements: B(t) = max(B_min, B_target * (R_current / R_required))
     */
    override fun shouldContinueLoading(
        playbackPositionUs: Long,
        bufferedDurationUs: Long,
        playbackSpeed: Float
    ): Boolean {
        val currentBitrate = bandwidthMeter.bitrateEstimate
        val ratio = if (currentBitrate > 0) {
            currentBitrate.toFloat() / requiredBitrateBps.toFloat()
        } else {
            1.0f
        }

        // Apply mathematical formula
        val calculatedBufferMs = (targetBufferMs * ratio).toInt()
        val dynamicTargetBufferMs = calculatedBufferMs.coerceIn(minBufferMs, maxBufferMs)
        
        val dynamicTargetBufferUs = dynamicTargetBufferMs * 1000L
        val shouldLoad = bufferedDurationUs < dynamicTargetBufferUs

        if (com.nate.app.BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "Bandwidth: $currentBitrate bps, Ratio: $ratio, " +
                "Buffered: ${bufferedDurationUs / 1000}ms, Target: ${dynamicTargetBufferMs}ms, Load: $shouldLoad"
            )
        }
        return shouldLoad
    }

    override fun shouldStartPlayback(
        bufferedDurationUs: Long,
        playbackSpeed: Float,
        rebuffering: Boolean,
        targetUs: Long
    ): Boolean {
        val minBufferUs = if (rebuffering) {
            bufferForPlaybackAfterRebufferMs * 1000L
        } else {
            bufferForPlaybackMs * 1000L
        }
        return minBufferUs <= 0 || bufferedDurationUs >= minBufferUs
    }

    private fun reset(resetAllocator: Boolean) {
        targetBufferSize = 0
        isBuffering = false
        if (resetAllocator) {
            allocator.reset()
        }
    }
}
