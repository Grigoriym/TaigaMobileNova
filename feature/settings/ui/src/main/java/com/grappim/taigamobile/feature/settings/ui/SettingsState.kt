package com.grappim.taigamobile.feature.settings.ui

import androidx.compose.runtime.Stable

@Stable
data class SettingsState(
    val appInfo: String,
    val serverUrl: String,
    val isAlertVisible: Boolean = false,
    val setIsAlertVisible: (Boolean) -> Unit
)
