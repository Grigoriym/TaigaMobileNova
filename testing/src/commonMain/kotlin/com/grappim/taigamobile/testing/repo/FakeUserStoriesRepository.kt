package com.grappim.taigamobile.testing.repo

import androidx.paging.PagingData
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.userstories.domain.UpdatedKanbanStory
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeUserStoriesRepository : UserStoriesRepository {

    var getEpicUserStoriesSimplifiedResult: ImmutableList<WorkItem> = persistentListOf()

    override fun getUserStoriesPaging(
        filters: FiltersData,
        query: String
    ): Flow<PagingData<WorkItem>> = flowOf(PagingData.empty())

    override suspend fun getEpicUserStoriesSimplified(epicId: Long): ImmutableList<WorkItem> =
        getEpicUserStoriesSimplifiedResult

    override suspend fun createUserStory(
        subject: String,
        description: String,
        status: Long?,
        swimlane: Long?
    ): WorkItem = error("not used in this test")

    override suspend fun getUserStory(id: Long): UserStory = error("not used in this test")

    var getUserStoriesResult: ImmutableList<UserStory> = persistentListOf()
    var getUserStoriesThrows: Throwable? = null

    override suspend fun getUserStories(
        assignedId: Long?,
        isClosed: Boolean?,
        isDashboard: Boolean?,
        watcherId: Long?,
        epicId: Long?,
        project: Long?,
        sprint: Any?
    ): ImmutableList<UserStory> {
        getUserStoriesThrows?.let { throw it }
        return getUserStoriesResult
    }

    override suspend fun patchData(
        version: Long,
        userStoryId: Long,
        payload: ImmutableMap<String, Any?>
    ): PatchedData = error("not used in this test")

    override suspend fun deleteUserStory(id: Long) = error("not used in this test")

    var bulkUpdateKanbanOrderThrows: Throwable? = null
    var bulkUpdateKanbanOrderCalled = false

    override suspend fun bulkUpdateKanbanOrder(
        statusId: Long,
        storyIds: List<Long>,
        swimlaneId: Long?,
        afterStoryId: Long?,
        beforeStoryId: Long?
    ): ImmutableList<UpdatedKanbanStory> {
        bulkUpdateKanbanOrderCalled = true
        bulkUpdateKanbanOrderThrows?.let { throw it }
        return persistentListOf()
    }
}
