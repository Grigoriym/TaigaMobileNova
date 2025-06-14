package io.eugenethedev.taigamobile.ui.screens.settings

import androidx.compose.runtime.Stable

@Stable
data class SettingsState(
    val appInfo: String,
    val serverUrl: String,
)