package com.grappim.taigamobile.feature.settings.ui.trustedcerts

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.core.domain.PendingCertTrust
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.storage.FakeTrustedCertStorage
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarController
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

// Desktop/JVM only: see docs/testing/compose-ui-test-spike.md.
//
// Proves a dialog visibility transition (closed -> open -> closed) driven by ViewModel state.
class TrustedCertificatesScreenTest {

    private val mainDispatcherRule = MainDispatcherRule()

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun clickingRevokeShowsDialogAndConfirmingRevokesEntry() = runComposeUiTest {
        val entry = PendingCertTrust(
            host = getRandomString(),
            subject = "CN=${getRandomString()}",
            issuer = "CN=${getRandomString()}",
            notBefore = "2026-01-01",
            notAfter = "2027-01-01",
            sha256Fingerprint = getRandomString()
        )
        val trustedCertStorage = FakeTrustedCertStorage()
        runBlocking { trustedCertStorage.trust(entry) }
        val viewModel = TrustedCertificatesViewModel(trustedCertStorage = trustedCertStorage)

        setContent {
            CompositionLocalProvider(LocalTopBarConfig provides TopBarController()) {
                TaigaMobilePreviewTheme {
                    TrustedCertificatesScreen(viewModel = viewModel)
                }
            }
        }

        onNodeWithText(entry.host).assertExists()
        onNodeWithText("Revoke trust").assertDoesNotExist()

        onNodeWithContentDescription("Revoke").performClick()

        onNodeWithText("Revoke trust").assertExists()

        onNodeWithText("Yes").performClick()

        onNodeWithText("Revoke trust").assertDoesNotExist()
        onNodeWithText(entry.host).assertDoesNotExist()
        assertEquals(entry.host to entry.sha256Fingerprint, trustedCertStorage.untrustCalledWith)
    }
}
