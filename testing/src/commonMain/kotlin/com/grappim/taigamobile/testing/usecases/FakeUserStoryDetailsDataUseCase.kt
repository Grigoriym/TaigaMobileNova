package com.grappim.taigamobile.testing.usecases

import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.userstories.domain.UserStoryDetailsData
import com.grappim.taigamobile.feature.userstories.domain.UserStoryDetailsDataUseCase
import com.grappim.taigamobile.testing.models.getUserStory
import com.grappim.taigamobile.testing.models.getUserStoryDetailsData

class FakeUserStoryDetailsDataUseCase : UserStoryDetailsDataUseCase {
    var getUserStoryDataResult: Result<UserStoryDetailsData> = Result.success(getUserStoryDetailsData())
    var deleteUserStoryResult: Result<Unit> = Result.success(Unit)
    var getUserStoryResult: UserStory = getUserStory()
    var getUserStoryDataCallCount = 0
    var deleteUserStoryCallCount = 0

    override suspend fun getUserStory(id: Long): UserStory = getUserStoryResult

    override suspend fun getUserStoryData(id: Long): Result<UserStoryDetailsData> {
        getUserStoryDataCallCount++
        return getUserStoryDataResult
    }

    override suspend fun deleteUserStory(id: Long): Result<Unit> {
        deleteUserStoryCallCount++
        return deleteUserStoryResult
    }
}
