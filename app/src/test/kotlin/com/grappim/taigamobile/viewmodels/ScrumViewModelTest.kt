package com.grappim.taigamobile.viewmodels

import com.grappim.taigamobile.core.domain.FiltersData
import com.grappim.taigamobile.scrum.ScrumViewModel
import com.grappim.taigamobile.ui.utils.ErrorResult
import com.grappim.taigamobile.ui.utils.SuccessResult
import com.grappim.taigamobile.viewmodels.utils.assertResultEquals
import com.grappim.taigamobile.viewmodels.utils.createDeniedException
import com.grappim.taigamobile.viewmodels.utils.notFoundException
import com.grappim.taigamobile.viewmodels.utils.testLazyPagingItems
import io.mockk.coEvery
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

class ScrumViewModelTest : BaseViewModelTest() {
    private lateinit var viewModel: ScrumViewModel

    @BeforeTest
    fun setup() {
        viewModel = ScrumViewModel(mockAppComponent)
    }

    @Test
    fun `test on open`(): Unit = runBlocking {
        val filtersData = FiltersData()

        coEvery { mockTaskRepository.getFiltersData(any(), true) } returns filtersData

        viewModel.onOpen()
        assertResultEquals(SuccessResult(filtersData), viewModel.filters.value)
    }

    @Test
    fun `test on open error`(): Unit = runBlocking {
        coEvery { mockTaskRepository.getFiltersData(any(), true) } throws notFoundException

        viewModel.onOpen()
        assertIs<ErrorResult<FiltersData>>(viewModel.filters.value)
    }

    @Test
    fun `test select filters`(): Unit = runBlocking {
        val filtersData = FiltersData()

        viewModel.selectFilters(filtersData)
        assertIs<FiltersData>(viewModel.activeFilters.value)
    }

    @Test
    fun `test create sprint`(): Unit = runBlocking {
        val testName = "test name"
        val startLocalDate = LocalDate.of(2000, 1, 1)
        val endLocalDate = LocalDate.of(3000, 1, 1)

        coEvery {
            mockSprintsRepository.createSprint(
                neq(testName),
                any(),
                any()
            )
        } throws createDeniedException

        viewModel.createSprint(testName, startLocalDate, endLocalDate)
        assertResultEquals(SuccessResult(Unit), viewModel.createSprintResult.value)

        viewModel.createSprint(testName + "wrong", startLocalDate, endLocalDate)
        assertIs<ErrorResult<Unit>>(viewModel.createSprintResult.value)
    }

    @Test
    fun `test open sprints list`(): Unit = runBlocking {
        testLazyPagingItems(viewModel.openSprints) {
            mockSprintsRepository.getSprints(
                any(),
                eq(false)
            )
        }
    }

    @Test
    fun `test closed sprints list`(): Unit = runBlocking {
        testLazyPagingItems(viewModel.closedSprints) {
            mockSprintsRepository.getSprints(
                any(),
                eq(true)
            )
        }
    }

    @Test
    fun `test stories list with filters`(): Unit = runBlocking {
        val query = "query"
        testLazyPagingItems(viewModel.stories) {
            mockTaskRepository.getBacklogUserStories(
                any(), eq(
                    FiltersData()
                )
            )
        }
        viewModel.selectFilters(FiltersData(query = query))
        testLazyPagingItems(viewModel.stories) {
            mockTaskRepository.getBacklogUserStories(
                any(), eq(
                    FiltersData(query = query)
                )
            )
        }
    }
}