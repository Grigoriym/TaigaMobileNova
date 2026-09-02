package com.grappim.taigamobile.feature.settings.ui.user

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.storage.FakeServerStorage
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarController
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SettingsUserScreenTest {

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
    fun httpServerShowsUnencryptedConnectionWarning() = runComposeUiTest {
        val usersRepository = FakeUsersRepository().apply { getMeResult = getUser() }
        val serverStorage = FakeServerStorage().apply { server = "http://taiga.internal.example" }
        val viewModel = SettingsUserScreenViewModel(
            usersRepository = usersRepository,
            serverStorage = serverStorage
        )

        setContent {
            CompositionLocalProvider(LocalTopBarConfig provides TopBarController()) {
                TaigaMobilePreviewTheme {
                    SettingsUserScreen(viewModel = viewModel)
                }
            }
        }

        onNodeWithTag(UNENCRYPTED_CONNECTION_WARNING_TEST_TAG).assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun httpsServerHidesUnencryptedConnectionWarning() = runComposeUiTest {
        val usersRepository = FakeUsersRepository().apply { getMeResult = getUser() }
        val serverStorage = FakeServerStorage().apply { server = "https://taiga.internal.example" }
        val viewModel = SettingsUserScreenViewModel(
            usersRepository = usersRepository,
            serverStorage = serverStorage
        )

        setContent {
            CompositionLocalProvider(LocalTopBarConfig provides TopBarController()) {
                TaigaMobilePreviewTheme {
                    SettingsUserScreen(viewModel = viewModel)
                }
            }
        }

        onNodeWithTag(UNENCRYPTED_CONNECTION_WARNING_TEST_TAG).assertDoesNotExist()
    }
}
