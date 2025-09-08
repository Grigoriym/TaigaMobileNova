package com.grappim.taigamobile.feature.issues.ui

import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import com.grappim.taigamobile.feature.issues.ui.list.IssuesViewModel
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.getFiltersDataDTO
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

    private val mockFilters = FiltersDataDTO()
    private val mockProjectId = 1L

    @Before
    fun setup() {
        every { taigaStorage.currentProjectIdFlow } returns flowOf(mockProjectId)
        every { issuesRepository.refreshIssues() } just Runs

        every { session.issuesFilters } returns MutableStateFlow(mockFilters)

        coEvery {
            filtersRepository.getFiltersDataResultOld(CommonTaskType.Issue)
        } returns Result.success(mockFilters)

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
            assertEquals(FiltersDataDTO(), state.activeFilters)
        }
    }

    @Test
    fun `on selectFilters, changes filters in session`() = runTest {
        val newFilters = getFiltersDataDTO()

        every { session.changeIssuesFilters(newFilters) } just Runs

        viewModel.state.value.selectFilters(newFilters)

        verify { session.changeIssuesFilters(newFilters) }
    }

    @Test
    fun `on filters with success, emits filters data`() = runTest {
        val filters = getFiltersDataDTO()
        coEvery {
            filtersRepository.getFiltersDataResultOld(CommonTaskType.Issue)
        } returns Result.success(filters)
        val activeFilters = viewModel.state.value.activeFilters.updateData(filters)

        every { session.changeIssuesFilters(activeFilters) } just Runs

        viewModel.filters.test {
            assertEquals(filters, awaitItem())

            verify { session.changeIssuesFilters(activeFilters) }
        }
    }

    @Test
    fun `on filters with failure, emits empty data`() = runTest {
        val filters = FiltersDataDTO()
        coEvery {
            filtersRepository.getFiltersDataResultOld(CommonTaskType.Issue)
        } returns Result.failure(testException)

        viewModel.filters.test {
            assertEquals(filters, awaitItem())

            verify(exactly = 0) { session.changeIssuesFilters(any()) }
        }
    }

    @Test
    fun `on retryLoadFilters, loads filters`() = runTest {
        val filters = getFiltersDataDTO()
        coEvery {
            filtersRepository.getFiltersDataResultOld(CommonTaskType.Issue)
        } returns Result.success(filters)
        val activeFilters = viewModel.state.value.activeFilters.updateData(filters)

        every { session.changeIssuesFilters(activeFilters) } just Runs

        viewModel.filters.test {
            assertEquals(filters, awaitItem())

            verify { session.changeIssuesFilters(activeFilters) }

            val newFilters = getFiltersDataDTO()
            coEvery {
                filtersRepository.getFiltersDataResultOld(CommonTaskType.Issue)
            } returns Result.success(newFilters)
            val newActiveFilters = viewModel.state.value.activeFilters.updateData(newFilters)

            every { session.changeIssuesFilters(newActiveFilters) } just Runs
            viewModel.state.value.retryLoadFilters()

            assertEquals(newFilters, awaitItem())
        }
    }
}
