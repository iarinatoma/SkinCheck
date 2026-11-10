package com.example.skincheck.data

import android.content.Context

// Remembers which user is logged in, even after the app is closed
object Session {
    private const val PREFS = "session"
    private const val KEY_USER_ID = "user_id"

    fun save(context: Context, userId: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putLong(KEY_USER_ID, userId).apply()
    }

    fun userId(context: Context): Long? {
        val id = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_USER_ID, -1L)
        return if (id == -1L) null else id
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(KEY_USER_ID).apply()
    }
}
