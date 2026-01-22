package com.grappim.taigamobile.feature.settings.ui.interfacescreen

import com.grappim.taigamobile.core.storage.ThemeSettings
import com.grappim.taigamobile.utils.ui.NativeText

data class SettingsInterfaceViewState(
    val themeSettings: ThemeSettings = ThemeSettings.default(),
    val onThemeChanged: (ThemeSettings) -> Unit = {},
    val themeDropDownTitle: NativeText = NativeText.Empty,
    val getThemeTitle: (ThemeSettings) -> NativeText = { NativeText.Empty }
)
