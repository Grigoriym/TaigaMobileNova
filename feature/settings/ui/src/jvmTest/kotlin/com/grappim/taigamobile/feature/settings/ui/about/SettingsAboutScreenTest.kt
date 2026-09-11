package com.grappim.taigamobile.feature.settings.ui.about

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.kit.testing.FakeAppInfoProvider
import com.grappim.kit.testing.FakeCrashReporter
import com.grappim.kit.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.kit.uikit.widgets.topbar.TopBarController
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import kotlin.test.Test

// Desktop/JVM only: see docs/testing/compose-ui-test-spike.md. This Screen has no
// SavedStateHandle/nav-route args.
class SettingsAboutScreenTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersAppInfoFromViewModel() = runComposeUiTest {
        val appInfoProvider = FakeAppInfoProvider().apply {
            versionNameToReturn = getRandomString()
            versionCodeToReturn = 42
            buildTypeToReturn = getRandomString()
        }
        val crashReporter = FakeCrashReporter()
        val viewModel = SettingsAboutScreenViewModel(appInfoProvider, crashReporter)

        setContent {
            CompositionLocalProvider(LocalTopBarConfig provides TopBarController()) {
                TaigaMobilePreviewTheme {
                    SettingsAboutScreen(viewModel = viewModel)
                }
            }
        }

        val expectedAppInfo = "${appInfoProvider.versionNameToReturn} - " +
            "${appInfoProvider.versionCodeToReturn} - ${appInfoProvider.buildTypeToReturn}"
        onNodeWithText(expectedAppInfo).assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersIssueSuggestionButton() = runComposeUiTest {
        val appInfoProvider = FakeAppInfoProvider()
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
