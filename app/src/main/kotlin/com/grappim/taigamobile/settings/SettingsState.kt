package com.grappim.taigamobile.settings

import androidx.compose.runtime.Stable

@Stable
data class SettingsState(
    val appInfo: String,
    val serverUrl: String,
)