package com.grappim.taigamobile.feature.issues.ui.list

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getWorkItem
import com.grappim.taigamobile.testing.repo.FakeFiltersRepository
import com.grappim.taigamobile.testing.repo.FakeIssuesRepository
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.storage.FakeFiltersStorage
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarController
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

// Desktop/JVM only: see docs/testing/compose-ui-test-spike.md. Paging sweep, task 21
// (improvement-plan.md) — last of the five-Screen sweep, same combine()+flatMapLatest shape as
// tasks 19/20 (session.issuesFilters + a local searchQuery MutableStateFlow, flatMapLatest'd into
// issuesRepository.getIssuesPaging(...)). No extra handling needed beyond
// MainDispatcherRule(UnconfinedTestDispatcher) + setContent, per tasks 19/20.
//
// FakeIssuesRepository.getIssuesPaging previously returned emptyFlow() (never emitted any
// PagingData), unlike every other paging fake's flowOf(PagingData.empty()) baseline — fixed to the
// common baseline before extending it to PagingData.from(...), same shape as
// FakeEpicsRepository/FakeSprintsRepository/FakeUserStoriesRepository.
//
// The "Add" (create issue) topbar action is NOT tested here, same reasoning as tasks 18/19/20: it
// lives in TopBarConfig.actions, rendered by an outer Scaffold this test doesn't compose.
//
// Item title assertion uses substring = true per task 19's finding: CommonTaskItem merges the title
// into one semantics Text node together with the ref number and indicator dots, so an exact
// onNodeWithText(workItem.title) match never finds it.
class IssuesScreenTest {

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
    fun rendersIssueFromPagingFlow() = runComposeUiTest {
        val workItem = getWorkItem()
        val issuesRepository = FakeIssuesRepository().apply {
            getIssuesPagingResult = persistentListOf(workItem)
        }
        val filtersStorage = FakeFiltersStorage()
        val filtersRepository = FakeFiltersRepository().apply {
            filtersDataResult = FiltersData()
        }
        val projectsRepository = FakeProjectsRepository().apply {
            permissions = persistentListOf()
        }
        val viewModel = IssuesViewModel(
            session = filtersStorage,
            issuesRepository = issuesRepository,
            filtersRepository = filtersRepository,
            projectsRepository = projectsRepository
        )

        setContent {
            CompositionLocalProvider(LocalTopBarConfig provides TopBarController()) {
                TaigaMobilePreviewTheme {
                    IssuesScreen(
                        showSnackbar = {},
                        goToCreateIssue = {},
                        goToIssue = { _, _ -> },
                        updateData = false,
                        viewModel = viewModel
                    )
                }
            }
        }

        onNodeWithText(workItem.title, substring = true).assertExists()
    }
}
