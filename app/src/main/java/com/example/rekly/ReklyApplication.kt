package com.example.rekly

import android.app.Application
import com.example.rekly.util.ThemeSettings

class ReklyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Before any screen opens, so the chosen theme shows from the first frame
        ThemeSettings(this).apply()
    }
}
