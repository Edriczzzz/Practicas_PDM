package com.example.practica3room.util

import android.content.Context
import android.content.SharedPreferences

class SyncPrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

    fun getLastSync(): Long = prefs.getLong("last_sync", 0)

    fun setLastSync(timestamp: Long) {
        prefs.edit().putLong("last_sync", timestamp).apply()
    }

    fun clearLastSync() {
        prefs.edit().remove("last_sync").apply()
    }
}