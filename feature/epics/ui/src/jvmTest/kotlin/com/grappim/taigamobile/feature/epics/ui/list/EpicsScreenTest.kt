package com.grappim.taigamobile.feature.epics.ui.list

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getWorkItem
import com.grappim.taigamobile.testing.repo.FakeEpicsRepository
import com.grappim.taigamobile.testing.repo.FakeFiltersRepository
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.storage.FakeFiltersStorage
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarController
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

// Desktop/JVM only: see docs/testing/compose-ui-test-spike.md.
//
// EpicsViewModel builds its paging Flow from session.epicsFilters + a local searchQuery
// MutableStateFlow, combine()'d and flatMapLatest'd into epicsRepository.getEpicsPaging(...). No
// extra handling is needed beyond MainDispatcherRule(UnconfinedTestDispatcher) + setContent: both
// source StateFlows already hold a value when EpicsViewModel.epics's property initializer runs, so
// combine() emits synchronously and flatMapLatest switches into the paging flow before the first
// frame settles.
//
// The "Add Epic" topbar action is not tested here: it lives in TopBarConfig.actions, rendered by an
// outer Scaffold this test doesn't compose.
//
// Item title assertions use onNodeWithText(workItem.title, substring = true): CommonTaskItem merges
// the title into one semantics Text node together with the ref number and indicator dots, so an
// exact match never finds it.
class EpicsScreenTest {

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
    fun rendersEpicFromPagingFlow() = runComposeUiTest {
        val workItem = getWorkItem()
        val epicsRepository = FakeEpicsRepository().apply {
            getEpicsPagingResult = persistentListOf(workItem)
        }
        val filtersStorage = FakeFiltersStorage()
        val filtersRepository = FakeFiltersRepository().apply {
            filtersDataResult = FiltersData()
        }
        val projectsRepository = FakeProjectsRepository().apply {
            permissions = persistentListOf()
        }
        val viewModel = EpicsViewModel(
            session = filtersStorage,
            epicsRepository = epicsRepository,
            filtersRepository = filtersRepository,
            projectsRepository = projectsRepository
        )

        setContent {
            CompositionLocalProvider(LocalTopBarConfig provides TopBarController()) {
                TaigaMobilePreviewTheme {
                    EpicsScreen(
                        showSnackbar = {},
                        goToCreateEpic = {},
                        goToEpic = { _, _, _ -> },
                        updateData = false,
                        viewModel = viewModel
                    )
                }
            }
        }

        onNodeWithText(workItem.title, substring = true).assertExists()
    }
}
