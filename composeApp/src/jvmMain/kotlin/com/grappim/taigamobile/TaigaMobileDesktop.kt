package com.grappim.taigamobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.grappim.taigamobile.core.logger.FileLogger
import com.grappim.taigamobile.core.logger.LogPriority
import com.grappim.taigamobile.core.logger.logcat
import com.grappim.taigamobile.core.storage.platform.appDataDir
import com.grappim.taigamobile.di.KoinApp
import com.grappim.taigamobile.main.TaigaAppContent
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.app_name
import com.grappim.taigamobile.uikit.utils.ScreenReadySignalController
import com.grappim.taigamobile.uikit.widgets.topbar.DesktopRefreshRegistry
import io.github.vinceglb.filekit.FileKit
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.stringResource
import org.koin.plugin.module.dsl.startKoin
import java.io.File

fun main() {
    FileLogger.install(File(appDataDir(), "taigamobile.log"))

    Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
        logcat(LogPriority.ERROR, tag = "UncaughtException", throwable = throwable) { "Uncaught exception" }
    }

    FileKit.init(appId = "com.grappim.taigamobile")

    val screenReadySignalController = ScreenReadySignalController(true)

    startKoin<KoinApp> { }

    val appIcon = BitmapPainter(
        Thread.currentThread().contextClassLoader.getResourceAsStream(
            "taiga-mobile-logo.png"
        )!!.readAllBytes().decodeToImageBitmap()
    )

    application {
        Window(
            icon = appIcon,
            onCloseRequest = ::exitApplication,
            title = stringResource(RString.app_name),
            alwaysOnTop = false,
            state = rememberWindowState(width = 600.dp, height = 800.dp),
            onPreviewKeyEvent = { event ->
                val isRefreshShortcut = event.type == KeyEventType.KeyDown &&
                    (event.key == Key.F5 || (event.isCtrlPressed && event.key == Key.R))
                if (isRefreshShortcut) {
                    DesktopRefreshRegistry.trigger()
                }
                isRefreshShortcut
            }
        ) {
            val rootFocusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) {
                rootFocusRequester.requestFocus()
            }
            Box(modifier = Modifier.fillMaxSize().focusRequester(rootFocusRequester).focusTarget()) {
                TaigaAppContent(screenReadySignalController)
            }
        }
    }
}
