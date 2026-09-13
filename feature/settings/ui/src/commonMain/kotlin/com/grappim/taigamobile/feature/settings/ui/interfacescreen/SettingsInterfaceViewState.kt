package com.grappim.taigamobile.feature.settings.ui.interfacescreen

import com.grappim.kit.uikit.NativeText
import com.grappim.taigamobile.core.storage.ThemeSettings

data class SettingsInterfaceViewState(
    val themeSettings: ThemeSettings = ThemeSettings.default(),
    val onThemeChanged: (ThemeSettings) -> Unit = {},
    val themeDropDownTitle: NativeText = NativeText.Empty,
    val getThemeTitle: (ThemeSettings) -> NativeText = { NativeText.Empty },
    val isCrashReportingAvailable: Boolean = false,
    val crashReportingEnabled: Boolean = false,
    val onCrashReportingToggle: (Boolean) -> Unit = {}
)
