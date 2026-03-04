package com.grappim.taigamobile.main

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.grappim.taigamobile.core.storage.ThemeSettings
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import androidx.compose.runtime.CompositionLocalProvider
import com.grappim.taigamobile.uikit.utils.LocalScreenReadySignal
import com.grappim.taigamobile.uikit.utils.ScreenReadySignalController
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TaigaAppContent(screenReadySignalController: ScreenReadySignalController) {
    val viewModel = koinViewModel<MainViewModel>()
    val theme by viewModel.theme.collectAsState()

    val darkTheme = when (theme) {
        ThemeSettings.Light -> false
        ThemeSettings.Dark -> true
        ThemeSettings.System -> isSystemInDarkTheme()
    }

    TaigaMobileTheme(darkTheme) {
        CompositionLocalProvider(
            LocalScreenReadySignal provides screenReadySignalController
        ) {
            MainContent(viewModel)
        }
    }
}
