package com.grappim.taigamobile.feature.settings.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.core.storage.auth.AuthStateManager
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getProjectSimple
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.storage.FakeAuthStorage
import com.grappim.taigamobile.testing.storage.FakeDatabaseWrapper
import com.grappim.taigamobile.testing.storage.FakeFiltersStorage
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SettingsScreenContentTest {

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

    // Settings owns the Log out action (moved from the nav drawer/rail, see
    // docs/issues/2026-08-26-tablet-nav-rail-logout-clipped.md) — dialog visibility transition
    // driven by SettingsViewModel state, same pilot pattern as TrustedCertificatesScreenTest.
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun clickingLogOutShowsDialogAndConfirmingLogsOut() = runComposeUiTest {
        val filtersStorage = FakeFiltersStorage()
        val projectsRepository = FakeProjectsRepository().apply {
            getCurrentProjectSimpleResult = getProjectSimple()
        }
        val authStateManager = AuthStateManager(
            filtersStorage = filtersStorage,
            taigaSessionStorage = FakeTaigaSessionStorage(),
            authStorage = FakeAuthStorage(),
            databaseWrapper = FakeDatabaseWrapper(),
            applicationScope = CoroutineScope(SupervisorJob())
        )
        val viewModel = SettingsViewModel(
            projectsRepository = projectsRepository,
            authStateManager = authStateManager
        )

        setContent {
            CompositionLocalProvider(LocalTopBarConfig provides TopBarController()) {
                TaigaMobilePreviewTheme {
                    SettingsScreen(
                        goToAboutScreen = {},
                        goToInterfaceScreen = {},
                        goToUserScreen = {},
                        goToAttributesScreen = {},
                        goToProjectDetailsScreen = {},
                        goToModulesScreen = {},
                        goToTrustedCertificatesScreen = {},
                        viewModel = viewModel
                    )
                }
            }
        }

        onNodeWithText("Are you sure you want to log out?").assertDoesNotExist()

        onNodeWithText("Log out").performClick()

        onNodeWithText("Are you sure you want to log out?").assertExists()

        onNodeWithText("Yes").performClick()

        onNodeWithText("Are you sure you want to log out?").assertDoesNotExist()
        assertTrue(filtersStorage.resetFiltersCalled)
    }
}
