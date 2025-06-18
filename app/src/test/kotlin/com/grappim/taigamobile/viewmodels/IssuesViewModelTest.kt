package com.grappim.taigamobile.viewmodels

import com.grappim.taigamobile.domain.entities.FiltersData
import com.grappim.taigamobile.issues.IssuesViewModel
import com.grappim.taigamobile.ui.utils.ErrorResult
import com.grappim.taigamobile.ui.utils.SuccessResult
import com.grappim.taigamobile.viewmodels.utils.assertResultEquals
import com.grappim.taigamobile.viewmodels.utils.notFoundException
import com.grappim.taigamobile.viewmodels.utils.testLazyPagingItems
import io.mockk.coEvery
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

class IssuesViewModelTest : BaseViewModelTest() {
    private lateinit var viewModel: IssuesViewModel

    @BeforeTest
    fun setup() {
        viewModel = IssuesViewModel(mockAppComponent)
    }

    @Test
    fun `test on open`(): Unit = runBlocking {
        val filtersData = FiltersData()

        coEvery { mockTaskRepository.getFiltersData(any()) } returns filtersData
        viewModel.onOpen()

        assertResultEquals(SuccessResult(filtersData), viewModel.filters.value)
    }

    @Test
    fun `test on open error`(): Unit = runBlocking {
        coEvery { mockTaskRepository.getFiltersData(any()) } throws notFoundException
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
    fun `test issues list with filters`(): Unit = runBlocking {
        val query = "query"
        testLazyPagingItems(viewModel.issues) {
            mockTaskRepository.getIssues(
                any(),
                eq(FiltersData())
            )
        }
        viewModel.selectFilters(FiltersData(query = query))
        testLazyPagingItems(viewModel.issues) {
            mockTaskRepository.getIssues(
                any(),
                eq(FiltersData(query = query))
            )
        }
    }
}