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

    var getUserStoriesPagingResult: ImmutableList<WorkItem> = persistentListOf()

    override fun getUserStoriesPaging(
        filters: FiltersData,
        query: String
    ): Flow<PagingData<WorkItem>> =
        if (getUserStoriesPagingResult.isEmpty()) {
            flowOf(PagingData.empty())
        } else {
            flowOf(PagingData.from(getUserStoriesPagingResult))
        }

    override suspend fun getEpicUserStoriesSimplified(epicId: Long): ImmutableList<WorkItem> =
        getEpicUserStoriesSimplifiedResult

    data class CreateUserStoryCall(
        val subject: String,
        val description: String,
        val status: Long?,
        val swimlane: Long?
    )

    var createUserStoryResult: WorkItem? = null
    var createUserStoryThrows: Throwable? = null
    val createUserStoryCalls: MutableList<CreateUserStoryCall> = mutableListOf()

    override suspend fun createUserStory(
        subject: String,
        description: String,
        status: Long?,
        swimlane: Long?
    ): WorkItem {
        createUserStoryCalls += CreateUserStoryCall(
            subject = subject,
            description = description,
            status = status,
            swimlane = swimlane
        )
        createUserStoryThrows?.let { throw it }
        return createUserStoryResult ?: error("createUserStoryResult not set")
    }

    var getUserStoryResult: UserStory? = null
    var getUserStoryThrows: Throwable? = null

    override suspend fun getUserStory(id: Long): UserStory {
        getUserStoryThrows?.let { throw it }
        return getUserStoryResult ?: error("getUserStoryResult not set")
    }

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

    var deleteUserStoryThrows: Throwable? = null
    var deleteUserStoryCalled = false

    override suspend fun deleteUserStory(id: Long) {
        deleteUserStoryCalled = true
        deleteUserStoryThrows?.let { throw it }
    }

    var bulkUpdateKanbanOrderThrows: Throwable? = null
    var bulkUpdateKanbanOrderCalled = false
    var bulkUpdateKanbanOrderStatusId: Long? = null
    var bulkUpdateKanbanOrderStoryIds: List<Long>? = null
    var bulkUpdateKanbanOrderSwimlaneId: Long? = null
    var bulkUpdateKanbanOrderAfterStoryId: Long? = null
    var bulkUpdateKanbanOrderBeforeStoryId: Long? = null

    override suspend fun bulkUpdateKanbanOrder(
        statusId: Long,
        storyIds: List<Long>,
        swimlaneId: Long?,
        afterStoryId: Long?,
        beforeStoryId: Long?
    ): ImmutableList<UpdatedKanbanStory> {
        bulkUpdateKanbanOrderCalled = true
        bulkUpdateKanbanOrderStatusId = statusId
        bulkUpdateKanbanOrderStoryIds = storyIds
        bulkUpdateKanbanOrderSwimlaneId = swimlaneId
        bulkUpdateKanbanOrderAfterStoryId = afterStoryId
        bulkUpdateKanbanOrderBeforeStoryId = beforeStoryId
        bulkUpdateKanbanOrderThrows?.let { throw it }
        return persistentListOf()
    }
}
