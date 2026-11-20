package com.example.rekly.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

/** The theme the user picked in the Profile; [SYSTEM] follows the device's light or dark setting. */
enum class ThemeMode(val key: String, val nightMode: Int) {
    SYSTEM("system", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    LIGHT("light", AppCompatDelegate.MODE_NIGHT_NO),
    DARK("dark", AppCompatDelegate.MODE_NIGHT_YES);

    companion object {
        /** An unknown or missing value falls back to the system theme. */
        fun fromKey(key: String?): ThemeMode = entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}

/** The theme belongs to the device, not to the logged user, so it lives apart from the session. */
class ThemeSettings(context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun mode(): ThemeMode = ThemeMode.fromKey(preferences.getString(KEY_MODE, null))

    fun save(mode: ThemeMode) {
        preferences.edit { putString(KEY_MODE, mode.key) }
    }

    /** Applies the saved theme; AppCompat recreates the open screens when the mode changes. */
    fun apply() {
        AppCompatDelegate.setDefaultNightMode(mode().nightMode)
    }

    private companion object {
        const val PREFERENCES_NAME = "theme"
        const val KEY_MODE = "mode"
    }
}
