package com.grappim.taigamobile.feature.scrum.ui.backlog

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getWorkItem
import com.grappim.taigamobile.testing.repo.FakeFiltersRepository
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.repo.FakeUserStoriesRepository
import com.grappim.taigamobile.testing.storage.FakeFiltersStorage
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarController
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

// Desktop/JVM only: see docs/testing/compose-ui-test-spike.md. Paging sweep, task 19
// (improvement-plan.md) — the first paging-list Screen whose ViewModel builds its paging Flow from a
// real combine() (session.scrumFilters + a local searchQuery MutableStateFlow) flatMapLatest'd into
// the repository call, rather than a class-level cold flow (tasks 14/17/18). No extra handling was
// needed beyond the established MainDispatcherRule(UnconfinedTestDispatcher) + setContent pattern:
// both source StateFlows already hold a value (session.scrumFilters defaults to FiltersData(),
// searchQuery defaults to ""), so combine() emits synchronously on collection and flatMapLatest
// switches into the paging flow before the first frame settles — same as a plain cold flow.
//
// The "Add User Story" topbar action is NOT tested here, same reasoning as task 18: it lives in
// TopBarConfig.actions, rendered by an outer Scaffold this test doesn't compose.
//
// The one real unknown here was unrelated to combine()/flatMapLatest: CommonTaskItem renders the
// title via CommonTaskTitle's "#ref title" pattern merged into one semantics Text node together with
// the ref number and indicator dots, so onNodeWithText(workItem.title) (exact match) never matches —
// only onNodeWithText(workItem.title, substring = true) does.
class ScrumBacklogScreenTest {

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
    fun rendersUserStoryFromPagingFlow() = runComposeUiTest {
        val workItem = getWorkItem()
        val userStoriesRepository = FakeUserStoriesRepository().apply {
            getUserStoriesPagingResult = persistentListOf(workItem)
        }
        val filtersStorage = FakeFiltersStorage()
        val filtersRepository = FakeFiltersRepository().apply {
            filtersDataResult = FiltersData()
        }
        val projectsRepository = FakeProjectsRepository().apply {
            permissions = persistentListOf()
        }
        val viewModel = ScrumBacklogViewModel(
            session = filtersStorage,
            userStoriesRepository = userStoriesRepository,
            filtersRepository = filtersRepository,
            projectsRepository = projectsRepository
        )

        setContent {
            CompositionLocalProvider(LocalTopBarConfig provides TopBarController()) {
                TaigaMobilePreviewTheme {
                    ScrumBacklogScreen(
                        updateData = false,
                        goToCreateUserStory = {},
                        navigateToTask = { _, _, _ -> },
                        viewModel = viewModel
                    )
                }
            }
        }

        onNodeWithText(workItem.title, substring = true).assertExists()
    }
}
