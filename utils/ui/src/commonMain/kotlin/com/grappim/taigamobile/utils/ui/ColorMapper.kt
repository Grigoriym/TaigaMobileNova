package com.grappim.taigamobile.utils.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.grappim.taigamobile.core.logger.logcat
import org.koin.core.annotation.Factory

@Factory
class ColorMapper {

    /**
     * backend does not handle alpha channel and will send an error if it is present
     */
    fun fromColorToString(color: Color): String {
        return formatColor(color.toArgb()).replace("#FF", "#")
    }

    fun fromStringToColor(string: String): Color = try {
        Color(fromStringToInt(string))
    } catch (e: Exception) {
        logcat(throwable = e) {
            "Error parsing color: $string"
        }
        Color.Transparent
    }
}

internal fun fromStringToInt(string: String): Int {
    val hex = string.removePrefix("#")
    val argb = when (hex.length) {
        6 -> (0xFF000000L or hex.toLong(16)).toInt()
        8 -> hex.toLong(16).toInt()
        else -> throw IllegalArgumentException("Unknown color format: $string")
    }
    return argb
}
