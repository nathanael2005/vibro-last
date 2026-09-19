package com.nate.core.common

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Centralized High-Performance Logger for NateTV / Vibro Native Android.
 * Formats every event with category icons, unified tag, and thread information.
 * Also publishes a real-time buffer of logs for on-screen TV diagnostics.
 */
object AppLogger {
    private const val MASTER_TAG = "NateTV"
    private const val MAX_ON_SCREEN_LOGS = 50
    private val logQueue = ConcurrentLinkedQueue<String>()
    private val _liveLogs = MutableStateFlow<List<String>>(emptyList())
    val liveLogs: StateFlow<List<String>> = _liveLogs.asStateFlow()

    private fun pushLog(formatted: String) {
        logQueue.add(formatted)
        while (logQueue.size > MAX_ON_SCREEN_LOGS) {
            logQueue.poll()
        }
        _liveLogs.value = logQueue.toList()
    }

    fun d(category: String, message: String) {
        val formatted = formatMessage(category, message)
        Log.d(MASTER_TAG, formatted)
        pushLog(formatted)
    }

    fun i(category: String, message: String) {
        val formatted = formatMessage(category, message)
        Log.i(MASTER_TAG, formatted)
        pushLog(formatted)
    }

    fun w(category: String, message: String, throwable: Throwable? = null) {
        val formatted = if (throwable != null) {
            Log.w(MASTER_TAG, formatMessage(category, message), throwable)
            formatMessage(category, "$message (${throwable.message})")
        } else {
            val f = formatMessage(category, message)
            Log.w(MASTER_TAG, f)
            f
        }
        pushLog(formatted)
    }

    fun e(category: String, message: String, throwable: Throwable? = null) {
        val formatted = if (throwable != null) {
            Log.e(MASTER_TAG, formatMessage(category, message), throwable)
            formatMessage(category, "$message (${throwable.message})")
        } else {
            val f = formatMessage(category, message)
            Log.e(MASTER_TAG, f)
            f
        }
        pushLog(formatted)
    }

    // Specialized high-visibility loggers
    fun player(message: String) = i("🎬 PLAYER", message)
    fun network(message: String) = i("🌐 NETWORK", message)
    fun stream(message: String) = i("⚡ STREAM", message)
    fun web(message: String) = i("🕸️ WEBVIEW", message)
    fun nav(message: String) = d("🧭 NAV", message)
    fun error(message: String, t: Throwable? = null) = e("❌ ERROR", message, t)

    private fun formatMessage(category: String, message: String): String {
        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
        return "[$time $category] $message"
    }
}
