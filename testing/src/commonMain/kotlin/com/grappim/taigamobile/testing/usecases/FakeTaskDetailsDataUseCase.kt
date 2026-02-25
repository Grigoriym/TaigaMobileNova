package com.grappim.taigamobile.testing.usecases

import com.grappim.taigamobile.feature.tasks.domain.TaskDetailsData
import com.grappim.taigamobile.feature.tasks.domain.TaskDetailsDataUseCase
import com.grappim.taigamobile.testing.models.getTaskDetailsData

class FakeTaskDetailsDataUseCase : TaskDetailsDataUseCase {
    var getTaskDataResult: Result<TaskDetailsData> = Result.success(getTaskDetailsData())
    var deleteTaskResult: Result<Unit> = Result.success(Unit)
    var getTaskDataCallCount = 0
    var deleteTaskCallCount = 0

    override suspend fun getTaskData(id: Long): Result<TaskDetailsData> {
        getTaskDataCallCount++
        return getTaskDataResult
    }

    override suspend fun deleteTask(id: Long): Result<Unit> {
        deleteTaskCallCount++
        return deleteTaskResult
    }
}
