package com.grappim.taigamobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.grappim.taigamobile.data.AppUpdateChecker
import com.grappim.taigamobile.data.UpdateState
import com.grappim.taigamobile.main.TaigaAppContent
import com.grappim.taigamobile.uikit.utils.ScreenReadySignalController
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val screenReadySignalController = ScreenReadySignalController()

    private val appUpdateChecker: AppUpdateChecker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                screenReadySignalController.isReady.value.not()
            }
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        FileKit.init(this)

        appUpdateChecker.checkAndRequestUpdate(this)
        observeUpdateState()

        setContent {
            TaigaAppContent(screenReadySignalController)
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateChecker.registerUpdateListener()
        appUpdateChecker.checkUpdateStateOnResume()
    }

    override fun onPause() {
        super.onPause()
        appUpdateChecker.unregisterUpdateListener()
    }

    private fun observeUpdateState() {
        lifecycleScope.launch {
            appUpdateChecker.updateState.collectLatest { state ->
                when (state) {
                    is UpdateState.UpdateDownloaded -> showRestartSnackbar()
                }
            }
        }
    }

    private fun showRestartSnackbar() {
        Snackbar.make(
            window.decorView.rootView,
            getString(R.string.app_update_downloaded),
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(getString(R.string.restart)) { appUpdateChecker.completeUpdate() }
            show()
        }
    }
}
