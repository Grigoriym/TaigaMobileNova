package com.grappim.taigamobile.feature.settings.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import kotlin.test.Test

class SettingsScreenContentTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun trustedCertificatesItemShownWhenSupported() = runComposeUiTest {
        setContent {
            TaigaMobilePreviewTheme {
                SettingsScreenContent(state = SettingsState(canSeeTrustedCertificates = true))
            }
        }

        onNodeWithContentDescription("Trusted Certificates Screen").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun trustedCertificatesItemHiddenWhenNotSupported() = runComposeUiTest {
        setContent {
            TaigaMobilePreviewTheme {
                SettingsScreenContent(state = SettingsState(canSeeTrustedCertificates = false))
            }
        }

        onNodeWithContentDescription("Trusted Certificates Screen").assertDoesNotExist()
    }
}
