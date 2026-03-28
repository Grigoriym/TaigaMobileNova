package com.grappim.taigamobile

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.grappim.taigamobile.di.KoinApp
import com.grappim.taigamobile.main.TaigaAppContent
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.app_name
import com.grappim.taigamobile.uikit.utils.ScreenReadySignalController
import io.github.vinceglb.filekit.FileKit
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.stringResource
import org.koin.plugin.module.dsl.startKoin

fun main() {
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
            state = rememberWindowState(width = 600.dp, height = 800.dp)
        ) {
            TaigaAppContent(screenReadySignalController)
        }
    }
}
