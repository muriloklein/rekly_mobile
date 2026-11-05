package com.example.rekly.util

import android.content.Context
import androidx.core.content.edit

class SessionManager(context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun save(userId: Long) {
        preferences.edit { putLong(KEY_USER_ID, userId) }
    }

    fun loggedUserId(): Long? =
        preferences.getLong(KEY_USER_ID, NO_USER).takeIf { it != NO_USER }

    fun clear() {
        preferences.edit { remove(KEY_USER_ID) }
    }

    companion object {
        private const val PREFERENCES_NAME = "session"
        private const val KEY_USER_ID = "user_id"
        private const val NO_USER = -1L
    }
}
