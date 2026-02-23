package com.grappim.taigamobile

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import com.grappim.taigamobile.core.logger.NSLogLogger
import com.grappim.taigamobile.di.KoinApp
import com.grappim.taigamobile.main.MainContent
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.LocalScreenReadySignal
import com.grappim.taigamobile.uikit.utils.ScreenReadySignalController
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.startKoin
import org.koin.plugin.module.dsl.startKoin
import platform.UIKit.UIViewController

@Suppress("unused")
fun MainViewController(): UIViewController = ComposeUIViewController(
    configure = {
        onFocusBehavior = OnFocusBehavior.DoNothing
    }
) {
    NSLogLogger.install()
    val screenReadySignalController =
        ScreenReadySignalController(true)
    startKoin<KoinApp> {  }
    TaigaMobileTheme {
        CompositionLocalProvider(
            LocalScreenReadySignal provides screenReadySignalController
        ) {
            MainContent(koinViewModel())
        }
    }
}
