package com.grappim.taigamobile.uikit.utils

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScreenReadySignalController {
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    fun signalReady() {
        _isReady.value = true
    }
}

/**
 * It is used to signal that the screen is ready to be rendered so that we can hide stop the splash screen
 * and don't show any initial screen when we want to bump on a screen with a backstack, e.g.
 * If we logged in, but didn't select a project yet, then in the next start of the app
 * we want to show the project selector screen while preserving the Login screen in the backstack so that
 * we could back there, and this signal helps us to not show the Login screen but the Project Selector after
 * splash screen
 */
val LocalScreenReadySignal = staticCompositionLocalOf<ScreenReadySignalController> {
    error("No ScreenReadySignalController provided")
}
