package com.grappim.taigamobile.utils.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import timber.log.Timber

/**
 * gray, because api returns null instead of gray -_-
 * #A9AABC is the default color from the front
 */
fun String?.fixNullColor() = if (this == null || this.isEmpty()) {
    "#A9AABC"
} else {
    this
}

@Deprecated("use ColorMapper")
fun Color.toHex(): String = "#%08X".format(toArgb()).replace("#FF", "#")

// calculate optimal text color for colored background background
fun Color.textColor() = if (luminance() < 0.5) Color.White else Color.Black

@Deprecated("use ColorMapper")
fun String.toColor(): Color = try {
    Color(this.toColorInt())
} catch (e: Exception) {
    Timber.e(e)
    Color.Transparent
}
