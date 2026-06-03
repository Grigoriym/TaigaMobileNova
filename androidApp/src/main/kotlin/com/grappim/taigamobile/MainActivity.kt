package com.grappim.taigamobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.grappim.taigamobile.main.TaigaAppContent
import com.grappim.taigamobile.uikit.utils.ScreenReadySignalController
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init

class MainActivity : ComponentActivity() {

    private val screenReadySignalController = ScreenReadySignalController()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                screenReadySignalController.isReady.value.not()
            }
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        FileKit.init(this)

        setContent {
            TaigaAppContent(screenReadySignalController)
        }
    }
}
