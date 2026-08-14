package com.grappim.taigamobile.feature.settings.ui.about

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.testing.FakeAppInfoProvider
import com.grappim.taigamobile.testing.FakeCrashReporter
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarController
import kotlin.test.Test

// Desktop/JVM only: see docs/testing/compose-ui-test-spike.md. Feature-level Screen pilot (task 12,
// improvement-plan.md) — this Screen has no SavedStateHandle/nav-route args; see the task's Result
// note for what a route-carrying Screen needs differently.
class SettingsAboutScreenTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersAppInfoFromViewModel() = runComposeUiTest {
        val appInfoProvider = FakeAppInfoProvider().apply { appInfoToReturn = getRandomString() }
        val crashReporter = FakeCrashReporter()
        val viewModel = SettingsAboutScreenViewModel(appInfoProvider, crashReporter)

        setContent {
            CompositionLocalProvider(LocalTopBarConfig provides TopBarController()) {
                TaigaMobilePreviewTheme {
                    SettingsAboutScreen(viewModel = viewModel)
                }
            }
        }

        onNodeWithText(appInfoProvider.appInfoToReturn).assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersIssueSuggestionButton() = runComposeUiTest {
        val appInfoProvider = FakeAppInfoProvider().apply { appInfoToReturn = getRandomString() }
        val crashReporter = FakeCrashReporter()
        val viewModel = SettingsAboutScreenViewModel(appInfoProvider, crashReporter)

        setContent {
            CompositionLocalProvider(LocalTopBarConfig provides TopBarController()) {
                TaigaMobilePreviewTheme {
                    SettingsAboutScreen(viewModel = viewModel)
                }
            }
        }

        onNodeWithText("Issue / Suggestion").assertExists()
    }
}
