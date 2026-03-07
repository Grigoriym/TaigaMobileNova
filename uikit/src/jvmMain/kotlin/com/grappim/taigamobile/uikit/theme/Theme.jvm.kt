package com.grappim.taigamobile.uikit.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun colorScheme(darkTheme: Boolean): ColorScheme = if (darkTheme) DarkColorPalette else LightColorPalette
