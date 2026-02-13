package com.grappim.taigamobile.main

import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.grappim.taigamobile.core.storage.ThemeSettings
import com.grappim.taigamobile.uikit.FilePicker
import com.grappim.taigamobile.uikit.LocalFilePicker
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.LocalScreenReadySignal
import com.grappim.taigamobile.uikit.utils.ScreenReadySignalController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val fileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> filePicker.filePicked(uri) }

    private val filePicker: FilePicker = object : FilePicker() {
        override fun requestFile(onFilePicked: (Uri?) -> Unit) {
            super.requestFile(onFilePicked)
            fileLauncher.launch("*/*")
        }
    }

    private val viewModel: MainViewModel by viewModels()
    private val screenReadySignalController = ScreenReadySignalController()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                screenReadySignalController.isReady.value.not()
            }
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val theme by viewModel.theme.collectAsState()

            val darkTheme = when (theme) {
                ThemeSettings.Light -> false
                ThemeSettings.Dark -> true
                ThemeSettings.System -> isSystemInDarkTheme()
            }

            TaigaMobileTheme(darkTheme) {
                CompositionLocalProvider(
                    LocalFilePicker provides filePicker,
                    LocalScreenReadySignal provides screenReadySignalController
                ) {
                    MainContent(viewModel)
                }
            }
        }
    }
}
