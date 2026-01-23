package com.grappim.taigamobile.utils.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.paging.compose.LazyPagingItems
import com.grappim.taigamobile.strings.RString
import timber.log.Timber
import kotlin.math.ln

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

// copy from library, because it is internal in library
@Deprecated("check it")
fun ColorScheme.surfaceColorAtElevationInternal(elevation: Dp): Color {
    if (elevation == 0.dp) return surface
    val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
    return primary.copy(alpha = alpha).compositeOver(surface)
}
