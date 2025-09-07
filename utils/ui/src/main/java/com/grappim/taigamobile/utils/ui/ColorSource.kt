package com.grappim.taigamobile.utils.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

sealed class ColorSource

data class StaticColor(val color: Color) : ColorSource()

data class StaticStringColor(val color: String?) : ColorSource()

data class DynamicColor(val colorProvider: @Composable () -> Color) : ColorSource()

@Composable
fun ColorSource.asColor(): Color = when (this) {
    is StaticColor -> color
    is DynamicColor -> colorProvider()
    is StaticStringColor -> color.fixNullColor().toColor()
}
