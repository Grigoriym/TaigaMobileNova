package com.grappim.taigamobile.uikit.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val DarkColorPalette = darkColorScheme(
    primary = taigaGreen,
    onPrimary = Color.White,
    secondary = taigaGreenDark,
    secondaryContainer = taigaGreenDark.copy(alpha = 0.5f),
    surfaceVariant = taigaGreen.copy(alpha = 0.1f),
    background = taigaDarkBackground,
    surface = taigaDarkBackground
)

internal val LightColorPalette = lightColorScheme(
    primary = taigaGreen,
    secondary = taigaGreenDark,
    secondaryContainer = taigaGreenLight.copy(alpha = 0.5f),
    surfaceVariant = taigaGreen.copy(alpha = 0.1f),
    background = taigaLightBackground,
    surface = taigaLightBackground
)

@Composable
expect fun colorScheme(darkTheme: Boolean): ColorScheme

@Composable
fun TaigaMobileTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colorScheme(darkTheme),
        typography = typography,
        content = content
    )
}

@Composable
fun TaigaMobilePreviewTheme(content: @Composable () -> Unit) {
    TaigaMobileTheme {
        Surface {
            content()
        }
    }
}
