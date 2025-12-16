package com.grappim.taigamobile.feature.userstories.domain

import androidx.paging.PagingData
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.coroutines.flow.Flow

interface UserStoriesRepository {
    fun getUserStoriesPaging(filters: FiltersData, query: String): Flow<PagingData<WorkItem>>

    fun refreshUserStories()

    //    suspend fun getAllUserStoriesOld(): List<CommonTaskExtended>
//    suspend fun getBacklogUserStories(page: Int, filters: FiltersData): List<CommonTask>

//    suspend fun getUserStoriesOld(
//        assignedId: Long? = null,
//        isClosed: Boolean? = null,
//        isDashboard: Boolean? = null,
//        watcherId: Long? = null,
//        epicId: Long? = null,
//        project: Long? = null,
//        sprint: Any? = null
//    ): List<CommonTask>

    suspend fun getEpicUserStoriesSimplified(epicId: Long): ImmutableList<WorkItem>

    suspend fun createUserStory(subject: String, description: String, status: Long?, swimlane: Long?): WorkItem

//    suspend fun getUserStoryByRefOld(projectId: Long, ref: Int): CommonTask

    suspend fun getUserStory(id: Long): UserStory

    suspend fun getUserStories(
        assignedId: Long? = null,
        isClosed: Boolean? = null,
        isDashboard: Boolean? = null,
        watcherId: Long? = null,
        epicId: Long? = null,
        project: Long? = null,
        sprint: Any? = null
    ): ImmutableList<UserStory>

    suspend fun patchData(version: Long, userStoryId: Long, payload: ImmutableMap<String, Any?>): PatchedData

    suspend fun deleteUserStory(id: Long)
}
