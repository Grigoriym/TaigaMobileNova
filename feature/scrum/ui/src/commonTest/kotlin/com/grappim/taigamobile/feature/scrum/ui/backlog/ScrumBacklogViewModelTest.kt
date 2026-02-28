package com.grappim.taigamobile.feature.scrum.ui.backlog

import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getStatusFilters
import com.grappim.taigamobile.testing.repo.FakeFiltersRepository
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.repo.FakeUserStoriesRepository
import com.grappim.taigamobile.testing.storage.FakeKmpSession
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ScrumBacklogViewModelTest {

    private val session = FakeKmpSession()
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

        assertEquals(filters, session.scrumFilters.value)
    }

    @Test
    fun `session scrumFilters change updates activeFilters in state`() {
        filtersRepository.filtersDataResult = FiltersData()
        createViewModel()

        val newFilters = FiltersData(statuses = persistentListOf(getStatusFilters()))
        session.changeScrumFilters(newFilters)

        assertEquals(newFilters, sut.state.value.activeFilters)
    }
}
