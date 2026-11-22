package com.grappim.taigamobile.feature.epics.ui.list

import app.cash.turbine.test
import com.grappim.kit.testing.MainDispatcherRule
import com.grappim.kit.uikit.NativeText
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.testing.models.getStatusFilters
import com.grappim.taigamobile.testing.repo.FakeEpicsRepository
import com.grappim.taigamobile.testing.repo.FakeFiltersRepository
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
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

        assertEquals(filters, session.currentEpicsFilters)
    }

    @Test
    fun `session epicsFilters change updates activeFilters in state`() {
        filtersRepository.filtersDataResult = FiltersData()
        createViewModel()

        val newFilters = FiltersData(statuses = persistentListOf(getStatusFilters()))
        session.changeEpicsFilters(newFilters)

        assertEquals(newFilters, sut.state.value.activeFilters)
    }

    @Test
    fun `filters data arriving before the stored selection is read keeps the stored selection`() {
        val stored = FiltersData(statuses = persistentListOf(getStatusFilters()))
        session.changeEpicsFilters(stored)
        session.storedFiltersLoaded.value = false
        filtersRepository.filtersDataResult = stored

        createViewModel()
        session.storedFiltersLoaded.value = true

        assertEquals(stored, session.currentEpicsFilters)
        assertEquals(stored, sut.state.value.activeFilters)
    }

    @Test
    fun `refreshed counts on the selected filters do not reload the list`() = runTest {
        val selected = getStatusFilters()
        val stored = FiltersData(statuses = persistentListOf(selected))
        session.changeEpicsFilters(stored)
        filtersRepository.filtersDataResult = stored
        createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { sut.epics.collect {} }

        filtersRepository.filtersDataResult =
            FiltersData(statuses = persistentListOf(selected.copy(count = selected.count + 1)))
        sut.state.value.retryLoadFilters()

        assertEquals(listOf(stored to ""), epicsRepository.getEpicsPagingCalls)
    }

    @Test
    fun `quick search keystrokes load the list once, with the last query`() = runTest {
        filtersRepository.filtersDataResult = FiltersData()
        createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { sut.epics.collect {} }

        sut.state.value.onSetQuery("i")
        sut.state.value.onSetQuery("is")
        sut.state.value.onSetQuery("iss")
        advanceTimeBy(SEARCH_QUERY_DEBOUNCE_MILLIS + 1)

        assertEquals(listOf("", "iss"), epicsRepository.getEpicsPagingCalls.map { it.second })
    }
}
