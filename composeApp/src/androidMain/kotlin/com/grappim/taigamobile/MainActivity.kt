package com.grappim.taigamobile

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.grappim.taigamobile.composeapp.main.MainContent
import com.grappim.taigamobile.composeapp.main.MainViewModel
import com.grappim.taigamobile.core.storage.ThemeSettings
import com.grappim.taigamobile.uikit.FilePicker
import com.grappim.taigamobile.uikit.LocalFilePicker
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.LocalScreenReadySignal
import com.grappim.taigamobile.uikit.utils.ScreenReadySignalController
import org.koin.compose.viewmodel.koinActivityViewModel

class MainActivity : ComponentActivity() {

    private val fileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> filePicker.filePicked(uri) }

    private val filePicker: FilePicker = object : FilePicker() {
        override fun requestFile(onFilePicked: (Uri?) -> Unit) {
            super.requestFile(onFilePicked)
            fileLauncher.launch("*/*")
        }
    }


    private val screenReadySignalController =
        ScreenReadySignalController()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                screenReadySignalController.isReady.value.not()
            }
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel = koinActivityViewModel<MainViewModel>()
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
