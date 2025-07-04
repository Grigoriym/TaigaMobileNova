package com.grappim.taigamobile.feature.settings.ui

import androidx.compose.runtime.Stable
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.storage.ThemeSettings
import com.grappim.taigamobile.utils.ui.NativeText

@Stable
data class SettingsState(
    val appInfo: String,
    val serverUrl: String,
    val user: User? = null,
    val isLoading: Boolean = false,
    val themeSettings: ThemeSettings = ThemeSettings.default(),
    val onThemeChanged: (ThemeSettings) -> Unit,
    val themeDropDownTitle: NativeText = NativeText.Empty,
    val getThemeTitle: (ThemeSettings) -> NativeText = { NativeText.Empty },
    val showSnackbar: (NativeText) -> Unit
)
