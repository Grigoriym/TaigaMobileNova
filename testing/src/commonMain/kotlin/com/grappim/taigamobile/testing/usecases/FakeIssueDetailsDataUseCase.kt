package com.grappim.taigamobile.testing.usecases

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.issues.domain.IssueDetailsData
import com.grappim.taigamobile.feature.issues.domain.IssueDetailsDataUseCase
import com.grappim.taigamobile.feature.issues.domain.UpdateSprintData
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.testing.models.getIssueDetailsData

class FakeIssueDetailsDataUseCase : IssueDetailsDataUseCase {
    var getIssueDataResult: Result<IssueDetailsData> = Result.success(getIssueDetailsData())
    var updateSprintResult: Result<UpdateSprintData> = Result.success(
        UpdateSprintData(
            patchedData = PatchedData(newVersion = 1L, dueDateStatus = null),
            sprint = null
        )
    )
    var getIssueDataCallCount = 0

    override suspend fun getIssueData(issueId: Long): Result<IssueDetailsData> {
        getIssueDataCallCount++
        return getIssueDataResult
    }

    override suspend fun updateSprint(
        sprintId: Long?,
        version: Long,
        workItemId: Long,
        commonTaskType: CommonTaskType,
    ): Result<UpdateSprintData> = updateSprintResult
}
