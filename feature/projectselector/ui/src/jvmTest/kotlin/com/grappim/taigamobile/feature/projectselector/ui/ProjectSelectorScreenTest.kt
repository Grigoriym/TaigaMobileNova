package com.grappim.taigamobile.feature.projectselector.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.cleaner.FakeDataCleaner
import com.grappim.taigamobile.testing.models.getProject
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.storage.FakeFiltersStorage
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarController
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

// Desktop/JVM only: see docs/testing/compose-ui-test-spike.md.
//
// This Screen's ViewModel exposes a `Flow<PagingData<Project>>` that the Screen collects with
// `collectAsLazyPagingItems()`. MainDispatcherRule's UnconfinedTestDispatcher runs init's
// session-storage collector to completion before setContent renders.
class ProjectSelectorScreenTest {

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
    fun rendersProjectFromPagingFlow() = runComposeUiTest {
        val project = getProject()
        val projectsRepository = FakeProjectsRepository().apply {
            fetchProjectsResult = persistentListOf(project)
        }
        val route = ProjectSelectorNavDestination(isFromLogin = false)
        val viewModel = ProjectSelectorViewModel(
            projectsRepository = projectsRepository,
            session = FakeFiltersStorage(),
            taigaSessionStorage = FakeTaigaSessionStorage(),
            dataCleaner = FakeDataCleaner(),
            route = route
        )

        setContent {
            CompositionLocalProvider(LocalTopBarConfig provides TopBarController()) {
                TaigaMobilePreviewTheme {
                    ProjectSelectorScreen(route = route, goBack = {}, onProjectSelect = {}, viewModel = viewModel)
                }
            }
        }

        onNodeWithText("${project.name} (${project.slug})").assertExists()
    }
}
