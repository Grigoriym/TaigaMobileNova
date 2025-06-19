package com.grappim.taigamobile.viewmodels

import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.createtask.CreateTaskViewModel
import com.grappim.taigamobile.ui.utils.ErrorResult
import com.grappim.taigamobile.ui.utils.SuccessResult
import com.grappim.taigamobile.viewmodels.utils.accessDeniedException
import com.grappim.taigamobile.viewmodels.utils.assertResultEquals
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

class CreateTaskViewModelTest : BaseViewModelTest() {
    private lateinit var viewModel: CreateTaskViewModel

    @BeforeTest
    fun setup() = runBlocking {
        viewModel = CreateTaskViewModel(mockAppComponent)
    }

    @Test
    fun `test create task`(): Unit = runBlocking {
        val mockCommonTaskType = mockk<CommonTaskType>(relaxed = true)
        val mockCommonTask = mockk<CommonTask>(relaxed = true)
        val title = "title"
        val description = "description"

        coEvery {
            mockTaskRepository.createCommonTask(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns mockCommonTask
        viewModel.createTask(mockCommonTaskType, title, description)
        assertResultEquals(SuccessResult(mockCommonTask), viewModel.creationResult.value)

        coEvery {
            mockTaskRepository.createCommonTask(
                any(),
                neq(title),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } throws accessDeniedException
        viewModel.createTask(mockCommonTaskType, title + "error", description)
        assertIs<ErrorResult<CommonTask>>(viewModel.creationResult.value)
    }
}