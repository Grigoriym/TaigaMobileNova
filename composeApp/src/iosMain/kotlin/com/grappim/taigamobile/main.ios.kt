package com.grappim.taigamobile

import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import com.grappim.taigamobile.core.logger.LogPriority
import com.grappim.taigamobile.core.logger.NSLogLogger
import com.grappim.taigamobile.core.logger.logcat
import com.grappim.taigamobile.di.KoinApp
import com.grappim.taigamobile.main.TaigaAppContent
import com.grappim.taigamobile.uikit.utils.ScreenReadySignalController
import org.koin.plugin.module.dsl.startKoin
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.setUnhandledExceptionHook

// PascalCase is required — this is the entry point Swift calls into.
@OptIn(ExperimentalNativeApi::class)
@Suppress("unused", "FunctionNaming")
fun MainViewController(): UIViewController = ComposeUIViewController(
    configure = {
        onFocusBehavior = OnFocusBehavior.DoNothing
    }
) {
    startKoin<KoinApp> { }

    NSLogLogger.install()
    setUnhandledExceptionHook { throwable ->
        logcat(LogPriority.ERROR, tag = "UncaughtException", throwable = throwable) { "Uncaught exception" }
    }
    TaigaAppContent(ScreenReadySignalController(true))
}
