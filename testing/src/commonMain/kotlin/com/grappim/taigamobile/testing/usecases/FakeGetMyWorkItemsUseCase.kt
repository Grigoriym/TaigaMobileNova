package com.grappim.taigamobile.testing.usecases

import com.grappim.taigamobile.feature.dashboard.domain.GetMyWorkItemsUseCase
import com.grappim.taigamobile.feature.workitem.domain.WorkItem

class FakeGetMyWorkItemsUseCase : GetMyWorkItemsUseCase {
    var result: Result<List<WorkItem>> = Result.success(emptyList())
    var callCount = 0

    override suspend fun getData(userId: Long, projectId: Long): Result<List<WorkItem>> {
        callCount++
        return result
    }
}
