package com.grappim.taigamobile.uikit.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.grappim.kit.uikit.KitPreviewTheme
import com.grappim.kit.uikit.KitTheme

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
    val scheme = colorScheme(darkTheme)
    KitTheme(
        lightColorScheme = scheme,
        darkColorScheme = scheme,
        typography = typography,
        darkTheme = false,
        content = content
    )
}

@Composable
fun TaigaMobilePreviewTheme(content: @Composable () -> Unit) {
    val scheme = colorScheme(isSystemInDarkTheme())
    KitPreviewTheme(
        lightColorScheme = scheme,
        darkColorScheme = scheme,
        typography = typography,
        darkTheme = false,
        content = content
    )
}
