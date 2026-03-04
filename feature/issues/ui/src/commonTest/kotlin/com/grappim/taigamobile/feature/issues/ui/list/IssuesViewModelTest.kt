package com.grappim.taigamobile.feature.issues.ui.list

import app.cash.turbine.test
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getStatusFilters
import com.grappim.taigamobile.testing.repo.FakeFiltersRepository
import com.grappim.taigamobile.testing.repo.FakeIssuesRepository
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

internal class IssuesViewModelTest {

    private val session = FakeFiltersStorage()
    private val issuesRepository = FakeIssuesRepository()
    private val filtersRepository = FakeFiltersRepository()
    private val projectsRepository = FakeProjectsRepository()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: IssuesViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = IssuesViewModel(
            session = session,
            issuesRepository = issuesRepository,
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
    fun `on init - ADD_ISSUE permission present - canCreateIssue is true`() {
        filtersRepository.filtersDataResult = FiltersData()
        projectsRepository.permissions = persistentListOf(TaigaPermission.ADD_ISSUE)

        createViewModel()

        assertTrue(sut.state.value.canCreateIssue)
    }

    @Test
    fun `on init - no ADD_ISSUE permission - canCreateIssue is false`() {
        filtersRepository.filtersDataResult = FiltersData()
        projectsRepository.permissions = persistentListOf()

        createViewModel()

        assertFalse(sut.state.value.canCreateIssue)
    }

    // --- search query ---

    @Test
    fun `setSearchQuery updates searchQuery flow`() {
        filtersRepository.filtersDataResult = FiltersData()
        createViewModel()

        val query = getRandomString()
        sut.state.value.setSearchQuery(query)

        assertEquals(query, sut.searchQuery.value)
    }

    // --- filter selection ---

    @Test
    fun `selectFilters updates session issuesFilters`() {
        filtersRepository.filtersDataResult = FiltersData()
        createViewModel()

        val filters = FiltersData(statuses = persistentListOf(getStatusFilters()))
        sut.state.value.selectFilters(filters)

        assertEquals(filters, session.issuesFilters.value)
    }

    @Test
    fun `session issuesFilters change updates activeFilters in state`() {
        filtersRepository.filtersDataResult = FiltersData()
        createViewModel()

        val newFilters = FiltersData(statuses = persistentListOf(getStatusFilters()))
        session.changeIssuesFilters(newFilters)

        assertEquals(newFilters, sut.state.value.activeFilters)
    }
}
