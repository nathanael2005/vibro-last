package com.example.data.local

import android.content.Context

object AdDraftStore {
    val data = mutableMapOf<String, Any>()
    
    fun getString(key: String, default: String): String = data[key] as? String ?: default
    fun getInt(key: String, default: Int): Int = data[key] as? Int ?: default
    fun getBoolean(key: String, default: Boolean): Boolean = data[key] as? Boolean ?: default
    
    fun clear() {
        data.clear()
    }

    fun saveDraft(context: Context) {
        try {
            val prefs = context.getSharedPreferences("marketplace_draft_prefs", Context.MODE_PRIVATE)
            val editor = prefs.edit().clear()
            data.forEach { (key, value) ->
                when (value) {
                    is String -> editor.putString(key, value)
                    is Int -> editor.putInt(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                }
            }
            editor.apply()
        } catch (e: Exception) {
            android.util.Log.e("AdDraftStore", "Failed to save draft: ${e.message}")
        }
    }

    fun loadDraft(context: Context) {
        try {
            val prefs = context.getSharedPreferences("marketplace_draft_prefs", Context.MODE_PRIVATE)
            prefs.all.forEach { (key, value) ->
                if (value != null) {
                    data[key] = value
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AdDraftStore", "Failed to load draft: ${e.message}")
        }
    }
}
