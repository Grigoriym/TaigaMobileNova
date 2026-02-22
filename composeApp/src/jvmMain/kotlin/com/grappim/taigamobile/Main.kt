package com.grappim.taigamobile

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.grappim.taigamobile.di.KoinApp
import com.grappim.taigamobile.main.MainContent
import com.grappim.taigamobile.main.MainViewModel
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.app_name
import io.github.vinceglb.filekit.FileKit
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.plugin.module.dsl.startKoin

fun main() {
    FileKit.init(appId = "com.grappim.taigamobile")

    startKoin<KoinApp> {
    }

    application {
        val viewModel = koinViewModel<MainViewModel>()
        Window(
            onCloseRequest = ::exitApplication,
            title = stringResource(RString.app_name),
            alwaysOnTop = true,
            state = rememberWindowState(width = 600.dp, height = 800.dp)
        ) {
            MainContent(viewModel)
        }
    }
}
