package com.grappim.taigamobile.utils.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import kotlin.math.ln

fun Color.toHex() = "#%08X".format(toArgb()).replace("#FF", "#")

// calculate optimal text color for colored background background
fun Color.textColor() = if (luminance() < 0.5) Color.White else Color.Black

fun String.toColor(): Color = try {
    Color(this.toColorInt())
} catch (e: Exception) {
    Color.Transparent
}

// copy from library, because it is internal in library
fun ColorScheme.surfaceColorAtElevationInternal(elevation: Dp): Color {
    if (elevation == 0.dp) return surface
    val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
    return primary.copy(alpha = alpha).compositeOver(surface)
}
