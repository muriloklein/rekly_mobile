package com.example.rekly.util

import android.content.res.ColorStateList
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.example.rekly.R
import com.example.rekly.data.local.DefaultCategories

/** Icon and colors of the category avatar; categories created by the user use the "Other" look. */
data class CategoryVisual(
    @param:DrawableRes val icon: Int,
    @param:ColorRes val container: Int,
    @param:ColorRes val onContainer: Int
)

fun categoryVisual(categoryId: Long): CategoryVisual = when (categoryId) {
    DefaultCategories.STREAMING ->
        CategoryVisual(R.drawable.ic_category_streaming, R.color.category_streaming, R.color.category_streaming_on)
    DefaultCategories.MUSIC ->
        CategoryVisual(R.drawable.ic_category_music, R.color.category_music, R.color.category_music_on)
    DefaultCategories.HOUSING ->
        CategoryVisual(R.drawable.ic_category_housing, R.color.category_housing, R.color.category_housing_on)
    DefaultCategories.TELECOM ->
        CategoryVisual(R.drawable.ic_category_telecom, R.color.category_telecom, R.color.category_telecom_on)
    DefaultCategories.HEALTH_WELLNESS ->
        CategoryVisual(R.drawable.ic_category_health, R.color.category_health, R.color.category_health_on)
    DefaultCategories.INSURANCE ->
        CategoryVisual(R.drawable.ic_category_insurance, R.color.category_insurance, R.color.category_insurance_on)
    else ->
        CategoryVisual(R.drawable.ic_category_other, R.color.category_other, R.color.category_other_on)
}

fun ImageView.showCategoryAvatar(categoryId: Long) {
    val visual = categoryVisual(categoryId)
    setImageResource(visual.icon)
    backgroundTintList = ColorStateList.valueOf(context.getColor(visual.container))
    imageTintList = ColorStateList.valueOf(context.getColor(visual.onContainer))
}
