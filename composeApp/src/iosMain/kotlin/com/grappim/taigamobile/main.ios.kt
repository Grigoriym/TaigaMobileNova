package com.grappim.taigamobile

import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import com.grappim.taigamobile.core.logger.NSLogLogger
import com.grappim.taigamobile.di.KoinApp
import com.grappim.taigamobile.main.TaigaAppContent
import com.grappim.taigamobile.uikit.utils.ScreenReadySignalController
import org.koin.plugin.module.dsl.startKoin
import platform.UIKit.UIViewController

@Suppress("unused")
fun MainViewController(): UIViewController = ComposeUIViewController(
    configure = {
        onFocusBehavior = OnFocusBehavior.DoNothing
    }
) {
    startKoin<KoinApp> { }

    NSLogLogger.install()
    TaigaAppContent(ScreenReadySignalController(true))
}
