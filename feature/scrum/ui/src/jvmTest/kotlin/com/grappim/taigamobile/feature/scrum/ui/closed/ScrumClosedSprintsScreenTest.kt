package com.grappim.taigamobile.feature.scrum.ui.closed

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getSprint
import com.grappim.taigamobile.testing.repo.FakeSprintsRepository
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarController
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

// Desktop/JVM only: see docs/testing/compose-ui-test-spike.md. Paging sweep, task 17
// (improvement-plan.md) — the simplest of the six paging-list Screens: one constructor dep, no
// init, no route. MainDispatcherRule's UnconfinedTestDispatcher lets cachedIn(viewModelScope)'s
// coroutine (which needs Dispatchers.Main) run, same reasoning as task 14's ProjectSelectorScreenTest.
class ScrumClosedSprintsScreenTest {

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
    fun rendersClosedSprintFromPagingFlow() = runComposeUiTest {
        val sprint = getSprint()
        val sprintsRepository = FakeSprintsRepository().apply {
            getSprintsPagingResult = persistentListOf(sprint)
        }
        val viewModel = ScrumClosedSprintsViewModel(sprintsRepository = sprintsRepository)

        setContent {
            CompositionLocalProvider(LocalTopBarConfig provides TopBarController()) {
                TaigaMobilePreviewTheme {
                    ScrumClosedSprintsScreen(
                        updateData = false,
                        goToSprint = {},
                        viewModel = viewModel
                    )
                }
            }
        }

        onNodeWithText(sprint.name).assertExists()
    }
}
