package com.example.rekly.util

import android.content.res.ColorStateList
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.google.android.material.color.MaterialColors
import com.google.android.material.R as MaterialR

/** Chip colors taken from the theme, so they follow the light and dark themes. */
enum class ChipTone(@param:AttrRes val container: Int, @param:AttrRes val onContainer: Int) {
    POSITIVE(MaterialR.attr.colorPrimaryContainer, MaterialR.attr.colorOnPrimaryContainer),
    NEUTRAL(MaterialR.attr.colorSecondaryContainer, MaterialR.attr.colorOnSecondaryContainer),
    ATTENTION(MaterialR.attr.colorTertiaryContainer, MaterialR.attr.colorOnTertiaryContainer),
    ERROR(MaterialR.attr.colorErrorContainer, MaterialR.attr.colorOnErrorContainer)
}

fun TextView.showChip(@StringRes text: Int, tone: ChipTone) {
    setText(text)
    backgroundTintList = ColorStateList.valueOf(MaterialColors.getColor(this, tone.container))
    setTextColor(MaterialColors.getColor(this, tone.onContainer))
    isVisible = true
}
