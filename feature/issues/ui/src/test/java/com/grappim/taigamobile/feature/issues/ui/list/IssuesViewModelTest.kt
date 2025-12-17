package com.grappim.taigamobile.feature.issues.ui.list

import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.getFiltersData
import com.grappim.taigamobile.testing.testException
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class IssuesViewModelTest {

    @get:Rule
    val coroutineRule = MainDispatcherRule()

    private val session: Session = mockk()
    private val issuesRepository: IssuesRepository = mockk()
    private val filtersRepository: FiltersRepository = mockk()
    private val taigaStorage: TaigaStorage = mockk()

    private lateinit var viewModel: IssuesViewModel

    private val mockFilters = FiltersData()
    private val mockProjectId = 1L

    @Before
    fun setup() {
        every { taigaStorage.currentProjectIdFlow } returns flowOf(mockProjectId)
        every { issuesRepository.refreshIssues() } just Runs

        every { session.issuesFilters } returns MutableStateFlow(mockFilters)

        coEvery {
            filtersRepository.getFiltersData(CommonTaskType.Issue)
        } returns mockFilters

        viewModel = IssuesViewModel(
            session = session,
            issuesRepository = issuesRepository,
            filtersRepository = filtersRepository,
            taigaStorage = taigaStorage
        )
    }

    @Test
    fun `initial state should have correct default values`() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isFiltersError)
            assertFalse(state.isFiltersLoading)
            assertEquals(FiltersData(), state.activeFilters)
        }
    }

    @Test
    fun `on selectFilters, changes filters in session`() = runTest {
        val newFilters = getFiltersData()

        every { session.changeIssuesFilters(newFilters) } just Runs

        viewModel.state.value.selectFilters(newFilters)

        verify { session.changeIssuesFilters(newFilters) }
    }

    @Test
    fun `on filters with success, emits filters data`() = runTest {
        val filters = getFiltersData()
        coEvery {
            filtersRepository.getFiltersData(CommonTaskType.Issue)
        } returns filters
        val activeFilters = viewModel.state.value.activeFilters.updateData(filters)

        every { session.changeIssuesFilters(activeFilters) } just Runs

        viewModel.filters.test {
            assertEquals(filters, awaitItem())

            verify { session.changeIssuesFilters(activeFilters) }
        }
    }

    @Test
    fun `on filters with failure, emits empty data`() = runTest {
        val filters = FiltersData()
        coEvery {
            filtersRepository.getFiltersData(CommonTaskType.Issue)
        } throws testException

        viewModel.filters.test {
            assertEquals(filters, awaitItem())

            verify(exactly = 0) { session.changeIssuesFilters(any()) }
        }
    }
}
