package com.grappim.taigamobile.utils.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import timber.log.Timber
import javax.inject.Inject

class ColorMapper @Inject constructor() {

    /**
     * backend does not handle alpha channel and will send an error if it is present
     */
    fun fromColorToString(color: Color): String = "#%08X".format(color.toArgb()).replace("#FF", "#")

    fun fromStringToColor(string: String): Color = try {
        Color(string.toColorInt())
    } catch (e: Exception) {
        Timber.e(e)
        Color.Transparent
    }
}
