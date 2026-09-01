package com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.feature.projects.domain.ProjectValueItem
import com.grappim.taigamobile.feature.projects.domain.ProjectValueType
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.repo.FakeProjectValuesRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarController
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

// Desktop/JVM only: see docs/testing/compose-ui-test-spike.md.
//
// The route is built directly and passed via constructor-parameter injection, the same way every
// ViewModel unit test for this class builds it. MainDispatcherRule's UnconfinedTestDispatcher makes
// the init block's load finish before the ViewModel constructor returns, so setContent renders the
// already-loaded state with no polling needed.
class ProjectValuesScreenTest {

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
    fun rendersItemsLoadedForTypeFromRoute() = runComposeUiTest {
        val type = ProjectValueType.US_STATUSES
        val item = ProjectValueItem(
            id = getRandomLong(),
            name = getRandomString(),
            order = 0,
            project = getRandomLong()
        )
        val repository = FakeProjectValuesRepository().apply {
            getProjectValuesResult = persistentListOf(item)
        }
        val route = ProjectValuesNavDestination(typeName = type.name)
        val viewModel = ProjectValuesViewModel(
            route = route,
            repository = repository,
            sessionStorage = FakeTaigaSessionStorage()
        )

        setContent {
            CompositionLocalProvider(LocalTopBarConfig provides TopBarController()) {
                TaigaMobilePreviewTheme {
                    ProjectValuesScreen(route = route, showSnackbar = {}, viewModel = viewModel)
                }
            }
        }

        onNodeWithText(item.name).assertExists()
        assertEquals(listOf(type), repository.getProjectValuesCalls)
    }
}
