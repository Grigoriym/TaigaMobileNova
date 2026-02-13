package com.grappim.taigamobile.utils.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import com.grappim.taigamobile.core.logger.logcat
import org.koin.core.annotation.Factory

@Factory
class ColorMapper {

    /**
     * backend does not handle alpha channel and will send an error if it is present
     */
    fun fromColorToString(color: Color): String = "#%08X".format(color.toArgb()).replace("#FF", "#")

    fun fromStringToColor(string: String): Color = try {
        Color(string.toColorInt())
    } catch (e: Exception) {
        logcat(throwable = e) {
            "Error parsing color: $string"
        }
        Color.Transparent
    }
}