package com.example.rekly.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

// Includes the keyboard (ime) so the form can scroll above it in edge-to-edge
fun applyFormScreenInsets(root: View, toolbar: View) {
    ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
        view.updatePadding(left = bars.left, right = bars.right, bottom = bars.bottom)
        insets
    }
    ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
        view.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
        insets
    }
}
