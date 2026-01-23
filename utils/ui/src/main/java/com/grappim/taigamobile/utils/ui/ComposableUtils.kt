package com.grappim.taigamobile.utils.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import timber.log.Timber

/**
 * gray, because api returns null instead of gray -_-
 */
fun String?.fixNullColor() = if (this == null || this.isEmpty()) {
    "#A9AABC"
} else {
    this
}

fun Color.toHex() = "#%08X".format(toArgb()).replace("#FF", "#")

// calculate optimal text color for colored background background
fun Color.textColor() = if (luminance() < 0.5) Color.White else Color.Black

fun String.toColor(): Color = try {
    Color(this.toColorInt())
} catch (e: Exception) {
    Timber.e(e)
    Color.Transparent
}
