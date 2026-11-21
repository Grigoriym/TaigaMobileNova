package com.grappim.taigamobile.main

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import kotlin.test.Test

// Desktop/JVM: the JVM actuals never block the local network, so the banner stays hidden here.
// The Android 17 path that shows it (permission denied) is only reachable on a device.
@OptIn(ExperimentalTestApi::class)
class LocalNetworkBannerJvmTest {

    @Test
    fun `signed in to a LAN server on desktop shows no banner`() = runComposeUiTest {
        setContent { TaigaMobilePreviewTheme { LocalNetworkBanner(serverUrl = "http://192.168.0.5:9000") } }

        onNodeWithText(OPEN_SETTINGS).assertDoesNotExist()
    }

    @Test
    fun `signed out shows no banner`() = runComposeUiTest {
        setContent { TaigaMobilePreviewTheme { LocalNetworkBanner(serverUrl = "") } }

        onNodeWithText(OPEN_SETTINGS).assertDoesNotExist()
    }

    private companion object {
        const val OPEN_SETTINGS = "Open settings"
    }
}
