package com.grappim.taigamobile.feature.epics.ui.list

import app.cash.turbine.test
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getStatusFilters
import com.grappim.taigamobile.testing.repo.FakeEpicsRepository
import com.grappim.taigamobile.testing.repo.FakeFiltersRepository
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.storage.FakeFiltersStorage
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class EpicsViewModelTest {

    private val session = FakeFiltersStorage()
    private val epicsRepository = FakeEpicsRepository()
    private val filtersRepository = FakeFiltersRepository()
    private val projectsRepository = FakeProjectsRepository()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: EpicsViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = EpicsViewModel(
            session = session,
            epicsRepository = epicsRepository,
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
    fun `on init - loadFiltersData failure - snackbar shown and filtersError set`() = runTest {
        // filtersDataResult is null by default, which triggers a throw in getFiltersData

        createViewModel()

        sut.snackBarMessage.test {
            assertTrue(awaitItem() !is NativeText.Empty)
            cancelAndIgnoreRemainingEvents()
        }

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
    fun `on init - ADD_EPIC permission present - canAddEpic is true`() {
        filtersRepository.filtersDataResult = FiltersData()
        projectsRepository.permissions = persistentListOf(TaigaPermission.ADD_EPIC)

        createViewModel()

        assertTrue(sut.state.value.canAddEpic)
    }

    @Test
    fun `on init - no ADD_EPIC permission - canAddEpic is false`() {
        filtersRepository.filtersDataResult = FiltersData()
        projectsRepository.permissions = persistentListOf()

        createViewModel()

        assertFalse(sut.state.value.canAddEpic)
    }

    // --- search query ---

    @Test
    fun `onSetQuery updates searchQuery flow`() {
        filtersRepository.filtersDataResult = FiltersData()
        createViewModel()

        val query = getRandomString()
        sut.state.value.onSetQuery(query)

        assertEquals(query, sut.searchQuery.value)
    }

    // --- filter selection ---

    @Test
    fun `selectFilters updates session epicsFilters`() {
        filtersRepository.filtersDataResult = FiltersData()
        createViewModel()

        val filters = FiltersData(statuses = persistentListOf(getStatusFilters()))
        sut.state.value.selectFilters(filters)

        assertEquals(filters, session.epicsFilters.value)
    }

    @Test
    fun `session epicsFilters change updates activeFilters in state`() {
        filtersRepository.filtersDataResult = FiltersData()
        createViewModel()

        val newFilters = FiltersData(statuses = persistentListOf(getStatusFilters()))
        session.changeEpicsFilters(newFilters)

        assertEquals(newFilters, sut.state.value.activeFilters)
    }
}
