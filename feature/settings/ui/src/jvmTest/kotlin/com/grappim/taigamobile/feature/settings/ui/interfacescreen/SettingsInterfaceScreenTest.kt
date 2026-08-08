package com.grappim.taigamobile.feature.settings.ui.interfacescreen

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.core.storage.ThemeSettings
import com.grappim.taigamobile.testing.FakeCrashReporter
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarController
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

// Desktop/JVM only: see docs/testing/compose-ui-test-spike.md. Multi-state-source Screen pilot
// (task 16, improvement-plan.md) — proves two independently launched `onEach{}.launchIn` collectors
// (themeSettings, crashReportingEnabled) both reach the rendered UI in one test.
class SettingsInterfaceScreenTest {

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
    fun bothIndependentStateSourcesReachTheScreen() = runComposeUiTest {
        val taigaSessionStorage = FakeTaigaSessionStorage(
            themeSettings = ThemeSettings.Dark,
            crashReportingEnabled = true
        )
        val crashReporter = FakeCrashReporter().apply { isAvailable = true }
        val viewModel = SettingsInterfaceViewModel(
            taigaSessionStorage = taigaSessionStorage,
            crashReporter = crashReporter
        )

        setContent {
            CompositionLocalProvider(LocalTopBarConfig provides TopBarController()) {
                TaigaMobilePreviewTheme {
                    SettingsInterfaceScreen(viewModel = viewModel)
                }
            }
        }

        onNodeWithText("Dark").assertIsSelected()
        onNodeWithText("System").assertIsNotSelected()
        onNode(isToggleable()).assertIsOn()
    }
}
