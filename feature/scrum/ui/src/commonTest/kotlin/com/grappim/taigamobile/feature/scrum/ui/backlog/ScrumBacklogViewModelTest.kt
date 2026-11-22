package com.grappim.taigamobile.feature.scrum.ui.backlog

import com.grappim.kit.testing.MainDispatcherRule
import com.grappim.kit.uikit.NativeText
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.testing.models.getStatusFilters
import com.grappim.taigamobile.testing.repo.FakeFiltersRepository
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.repo.FakeUserStoriesRepository
import com.grappim.taigamobile.testing.storage.FakeFiltersStorage
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.utils.ui.SEARCH_QUERY_DEBOUNCE_MILLIS
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ScrumBacklogViewModelTest {

    private val session = FakeFiltersStorage()
    private val userStoriesRepository = FakeUserStoriesRepository()
    private val filtersRepository = FakeFiltersRepository()
    private val projectsRepository = FakeProjectsRepository()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: ScrumBacklogViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = ScrumBacklogViewModel(
            session = session,
            userStoriesRepository = userStoriesRepository,
            filtersRepository = filtersRepository,
            projectsRepository = projectsRepository
        )
    }

    // --- filters loading ---

    @Test
    fun `on init - loadFiltersData success - state has filters and isFiltersLoading false`() {
        val filters = FiltersData(statuses = persistentListOf(getStatusFilters()))
        filtersRepository.filtersDataResult = filters

        createViewModel()

        with(sut.state.value) {
            assertFalse(isFiltersLoading)
            assertEquals(NativeText.Empty, filtersError)
            assertEquals(filters, this.filters)
        }
    }

    @Test
    fun `on init - loadFiltersData failure - filtersError set and isFiltersLoading false`() {
        // filtersDataResult is null by default, which triggers an error in getFiltersData

        createViewModel()

        with(sut.state.value) {
            assertFalse(isFiltersLoading)
            assertTrue(filtersError !is NativeText.Empty)
        }
    }

    @Test
    fun `retryLoadFilters reloads filters data`() {
        filtersRepository.filtersDataResult = FiltersData()
        createViewModel()

        val newFilters = FiltersData(statuses = persistentListOf(getStatusFilters()))
        filtersRepository.filtersDataResult = newFilters

        sut.state.value.retryLoadFilters()

        assertEquals(newFilters, sut.state.value.filters)
    }

    // --- permissions ---

    @Test
    fun `on init - ADD_US permission present - canAddUserStory is true`() {
        filtersRepository.filtersDataResult = FiltersData()
        projectsRepository.permissions = persistentListOf(TaigaPermission.ADD_US)

        createViewModel()

        assertTrue(sut.state.value.canAddUserStory)
    }

    @Test
    fun `on init - no ADD_US permission - canAddUserStory is false`() {
        filtersRepository.filtersDataResult = FiltersData()
        projectsRepository.permissions = persistentListOf()

        createViewModel()

        assertFalse(sut.state.value.canAddUserStory)
    }

    // --- search query ---

    @Test
    fun `onSetSearchQuery updates searchQuery flow`() {
        filtersRepository.filtersDataResult = FiltersData()
        createViewModel()

        val query = getRandomString()
        sut.state.value.onSetSearchQuery(query)

        assertEquals(query, sut.searchQuery.value)
    }

    // --- filter selection ---

    @Test
    fun `onSelectFilters updates session scrumFilters`() {
        filtersRepository.filtersDataResult = FiltersData()
        createViewModel()

        val filters = FiltersData(statuses = persistentListOf(getStatusFilters()))
        sut.state.value.onSelectFilters(filters)

        assertEquals(filters, session.currentScrumFilters)
    }

    @Test
    fun `session scrumFilters change updates activeFilters in state`() {
        filtersRepository.filtersDataResult = FiltersData()
        createViewModel()

        val newFilters = FiltersData(statuses = persistentListOf(getStatusFilters()))
        session.changeScrumFilters(newFilters)

        assertEquals(newFilters, sut.state.value.activeFilters)
    }

    @Test
    fun `filters data arriving before the stored selection is read keeps the stored selection`() {
        val stored = FiltersData(statuses = persistentListOf(getStatusFilters()))
        session.changeScrumFilters(stored)
        session.storedFiltersLoaded.value = false
        filtersRepository.filtersDataResult = stored

        createViewModel()
        session.storedFiltersLoaded.value = true

        assertEquals(stored, session.currentScrumFilters)
        assertEquals(stored, sut.state.value.activeFilters)
    }

    @Test
    fun `refreshed counts on the selected filters do not reload the list`() = runTest {
        val selected = getStatusFilters()
        val stored = FiltersData(statuses = persistentListOf(selected))
        session.changeScrumFilters(stored)
        filtersRepository.filtersDataResult = stored
        createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { sut.userStories.collect {} }

        filtersRepository.filtersDataResult =
            FiltersData(statuses = persistentListOf(selected.copy(count = selected.count + 1)))
        sut.state.value.retryLoadFilters()

        assertEquals(listOf(stored to ""), userStoriesRepository.getUserStoriesPagingCalls)
    }

    @Test
    fun `quick search keystrokes load the list once, with the last query`() = runTest {
        filtersRepository.filtersDataResult = FiltersData()
        createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { sut.userStories.collect {} }

        sut.state.value.onSetSearchQuery("i")
        sut.state.value.onSetSearchQuery("is")
        sut.state.value.onSetSearchQuery("iss")
        advanceTimeBy(SEARCH_QUERY_DEBOUNCE_MILLIS + 1)

        assertEquals(listOf("", "iss"), userStoriesRepository.getUserStoriesPagingCalls.map { it.second })
    }
}
