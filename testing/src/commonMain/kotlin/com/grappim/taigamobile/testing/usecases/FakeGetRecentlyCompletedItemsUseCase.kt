package com.grappim.taigamobile.testing.usecases

import com.grappim.taigamobile.feature.dashboard.domain.GetRecentlyCompletedItemsUseCase
import com.grappim.taigamobile.feature.workitem.domain.WorkItem

class FakeGetRecentlyCompletedItemsUseCase : GetRecentlyCompletedItemsUseCase {
    var result: Result<List<WorkItem>> = Result.success(emptyList())
    var callCount = 0

    override suspend fun getData(projectId: Long): Result<List<WorkItem>> {
        callCount++
        return result
    }
}
