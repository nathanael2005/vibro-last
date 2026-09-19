package com.nate.tv

import android.app.Application
import android.graphics.Bitmap
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class TvApplication : Application(), ImageLoaderFactory {

    @Inject
    lateinit var okHttpClient: OkHttpClient

    override fun onCreate() {
        super.onCreate()

        val prefs = getSharedPreferences("crash_reports", android.content.Context.MODE_PRIVATE)
        val lastCrash = prefs.getString("last_crash", null)
        if (lastCrash != null) {
            com.nate.core.common.AppLogger.error("🚨 LAST UNCAUGHT CRASH: $lastCrash")
            prefs.edit().remove("last_crash").apply()
        }

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val stackTrace = throwable.stackTraceToString()
            val shortMsg = "${throwable::class.java.simpleName}: ${throwable.message}\n${stackTrace.take(600)}"
            prefs.edit().putString("last_crash", shortMsg).commit()
            com.nate.core.common.AppLogger.e("FATAL", "Crash on ${thread.name}: ${throwable.message}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .bitmapConfig(Bitmap.Config.RGB_565) // 50% RAM reduction on TV
            .allowRgb565(true)
            .crossfade(false) // Zero animation overhead
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.15) // Keep RAM usage under 15%
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024) // Strict 50 MB limit protecting flash storage
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
    }
}
