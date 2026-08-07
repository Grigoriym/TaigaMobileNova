package com.grappim.taigamobile.feature.scrum.ui.open

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getSprint
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.repo.FakeSprintsRepository
import com.grappim.taigamobile.testing.utils.FakeDateTimeUtils
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarController
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

// Desktop/JVM only: see docs/testing/compose-ui-test-spike.md. Paging sweep, task 18
// (improvement-plan.md) — reuses task 17's feature/scrum/ui build wiring and FakeSprintsRepository
// extension. The isClosed fake gap task 17 flagged doesn't matter here since each test constructs
// its own FakeSprintsRepository instance.
//
// The "Add Sprint" topbar action -> EditSprintDialog interaction is NOT tested here: the action
// button lives in TopBarConfig.actions, which TopBarController only holds as state — the actual
// TopAppBar that renders those actions is composed by an outer Scaffold outside this Screen, so
// clicking it can't be exercised from a test that only renders ScrumOpenSprintsScreen in isolation.
class ScrumOpenSprintsScreenTest {

    private val mainDispatcherRule = MainDispatcherRule()

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel(sprintsRepository: FakeSprintsRepository, projectsRepository: FakeProjectsRepository) =
        ScrumOpenSprintsViewModel(
            sprintsRepository = sprintsRepository,
            projectsRepository = projectsRepository,
            dateTimeUtils = FakeDateTimeUtils()
        )

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersOpenSprintFromPagingFlow() = runComposeUiTest {
        val sprint = getSprint()
        val sprintsRepository = FakeSprintsRepository().apply {
            getSprintsPagingResult = persistentListOf(sprint)
        }
        val projectsRepository = FakeProjectsRepository().apply {
            permissions = persistentListOf()
        }
        val viewModel = createViewModel(sprintsRepository, projectsRepository)

        setContent {
            CompositionLocalProvider(LocalTopBarConfig provides TopBarController()) {
                TaigaMobilePreviewTheme {
                    ScrumOpenSprintsScreen(
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
