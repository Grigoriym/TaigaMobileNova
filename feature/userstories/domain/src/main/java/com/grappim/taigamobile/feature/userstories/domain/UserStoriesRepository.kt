package com.grappim.taigamobile.feature.userstories.domain

import androidx.paging.PagingData
import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskExtended
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.CustomFields
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.domain.patch.PatchedData
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.coroutines.flow.Flow

interface UserStoriesRepository {
    fun getUserStoriesPaging(filters: FiltersDataDTO): Flow<PagingData<CommonTask>>

    fun refreshUserStories()
    suspend fun getAllUserStories(): List<CommonTaskExtended>
    suspend fun getBacklogUserStories(page: Int, filters: FiltersDataDTO): List<CommonTask>

    suspend fun getUserStories(
        assignedId: Long? = null,
        isClosed: Boolean? = null,
        isDashboard: Boolean? = null,
        watcherId: Long? = null,
        epicId: Long? = null,
        project: Long? = null,
        sprint: Any? = null
    ): List<CommonTask>

    suspend fun createUserStory(
        project: Long,
        subject: String,
        description: String,
        status: Long?,
        swimlane: Long?
    ): CommonTaskResponse

    suspend fun getUserStoryByRefOld(projectId: Long, ref: Int): CommonTask

    suspend fun getUserStory(id: Long): UserStory
    suspend fun getUserStoryAttachments(taskId: Long): List<Attachment>

    suspend fun getCustomFields(id: Long): CustomFields

    suspend fun patchData(version: Long, userStoryId: Long, payload: ImmutableMap<String, Any?>): PatchedData

    suspend fun deleteUserStory(id: Long)
}
