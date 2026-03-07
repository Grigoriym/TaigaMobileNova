package com.grappim.taigamobile.testing.usecases

import com.grappim.taigamobile.feature.epics.domain.EpicColorUpdateData
import com.grappim.taigamobile.feature.epics.domain.EpicDetailsData
import com.grappim.taigamobile.feature.epics.domain.EpicDetailsDataUseCase
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.testing.models.getEpicDetailsData
import kotlinx.collections.immutable.persistentListOf

class FakeEpicDetailsDataUseCase : EpicDetailsDataUseCase {
    var getEpicDataResult: Result<EpicDetailsData> = Result.success(getEpicDetailsData())
    var changeEpicColorResult: Result<EpicColorUpdateData> = Result.success(
        EpicColorUpdateData(
            patchedData = PatchedData(newVersion = 1L, dueDateStatus = null),
            userStories = persistentListOf()
        )
    )
    var getEpicDataCallCount = 0

    override suspend fun getEpicData(epicId: Long): Result<EpicDetailsData> {
        getEpicDataCallCount++
        return getEpicDataResult
    }

    override suspend fun changeEpicColor(color: String, version: Long, epicId: Long): Result<EpicColorUpdateData> {
        return changeEpicColorResult
    }
}