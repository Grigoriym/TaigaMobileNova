package com.grappim.taigamobile.feature.userstories.domain

import androidx.paging.PagingData
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskExtended
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.coroutines.flow.Flow

interface UserStoriesRepository {
    fun getUserStoriesPaging(filters: FiltersDataDTO): Flow<PagingData<CommonTask>>

    fun refreshUserStories()

//    suspend fun getAllUserStoriesOld(): List<CommonTaskExtended>
    suspend fun getBacklogUserStories(page: Int, filters: FiltersDataDTO): List<CommonTask>

    suspend fun getUserStoriesOld(
        assignedId: Long? = null,
        isClosed: Boolean? = null,
        isDashboard: Boolean? = null,
        watcherId: Long? = null,
        epicId: Long? = null,
        project: Long? = null,
        sprint: Any? = null
    ): List<CommonTask>

    suspend fun getEpicUserStoriesSimplified(epicId: Long): ImmutableList<WorkItem>

    suspend fun createUserStory(
        project: Long,
        subject: String,
        description: String,
        status: Long?,
        swimlane: Long?
    ): CommonTaskResponse

    suspend fun getUserStoryByRefOld(projectId: Long, ref: Int): CommonTask

    suspend fun getUserStory(id: Long): UserStory

    suspend fun getUserStories(): ImmutableList<UserStory>

    suspend fun patchData(version: Long, userStoryId: Long, payload: ImmutableMap<String, Any?>): PatchedData

    suspend fun deleteUserStory(id: Long)
}
