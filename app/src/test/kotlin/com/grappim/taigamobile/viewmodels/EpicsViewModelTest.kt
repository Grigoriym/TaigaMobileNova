package com.grappim.taigamobile.viewmodels

import com.grappim.taigamobile.domain.entities.CommonTaskType
import com.grappim.taigamobile.domain.entities.FiltersData
import com.grappim.taigamobile.epics.EpicsViewModel
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

class EpicsViewModelTest : BaseViewModelTest() {
    private lateinit var viewModel: EpicsViewModel

    @BeforeTest
    fun setup() {
        viewModel = EpicsViewModel(mockAppComponent)
    }

    @Test
    fun `test on open`() = runBlocking {
        val filtersData = FiltersData()
        coEvery { mockTaskRepository.getFiltersData(CommonTaskType.Epic) } returns filtersData
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
    fun `test epics list with filters`(): Unit = runBlocking {
        val query = "query"
        testLazyPagingItems(viewModel.epics) {
            mockTaskRepository.getEpics(
                any(),
                eq(FiltersData())
            )
        }
        viewModel.selectFilters(FiltersData(query = query))
        testLazyPagingItems(viewModel.epics) {
            mockTaskRepository.getEpics(
                any(),
                eq(FiltersData(query = query))
            )
        }
    }
}
