package com.grappim.taigamobile.feature.userstories.domain

import androidx.paging.PagingData
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskExtended
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.FiltersData
import kotlinx.coroutines.flow.Flow

interface UserStoriesRepository {
    fun getUserStories(filters: FiltersData): Flow<PagingData<CommonTask>>
    suspend fun getAllUserStories(): List<CommonTaskExtended>
    suspend fun getBacklogUserStories(page: Int, filters: FiltersData): List<CommonTask>

    suspend fun getUserStories(
        assignedId: Long? = null,
        isClosed: Boolean? = null,
        isDashboard: Boolean? = null,
        watcherId: Long? = null,
        epicId: Long? = null
    ): List<CommonTask>

    suspend fun createUserStory(
        project: Long,
        subject: String,
        description: String,
        status: Long?,
        swimlane: Long?
    ): CommonTaskResponse

    suspend fun getUserStoryByRef(projectId: Long, ref: Int): CommonTask
}
