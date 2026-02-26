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
import kotlinx.coroutines.flow.Flow

class FakeUserStoriesRepository : UserStoriesRepository {
    override fun getUserStoriesPaging(
        filters: FiltersData,
        query: String
    ): Flow<PagingData<WorkItem>> {
        TODO("Not yet implemented")
    }

    override suspend fun getEpicUserStoriesSimplified(epicId: Long): ImmutableList<WorkItem> {
        TODO("Not yet implemented")
    }

    override suspend fun createUserStory(
        subject: String,
        description: String,
        status: Long?,
        swimlane: Long?
    ): WorkItem {
        TODO("Not yet implemented")
    }

    override suspend fun getUserStory(id: Long): UserStory {
        TODO("Not yet implemented")
    }

    override suspend fun getUserStories(
        assignedId: Long?,
        isClosed: Boolean?,
        isDashboard: Boolean?,
        watcherId: Long?,
        epicId: Long?,
        project: Long?,
        sprint: Any?
    ): ImmutableList<UserStory> {
        TODO("Not yet implemented")
    }

    override suspend fun patchData(
        version: Long,
        userStoryId: Long,
        payload: ImmutableMap<String, Any?>
    ): PatchedData {
        TODO("Not yet implemented")
    }

    override suspend fun deleteUserStory(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun bulkUpdateKanbanOrder(
        statusId: Long,
        storyIds: List<Long>,
        swimlaneId: Long?,
        afterStoryId: Long?,
        beforeStoryId: Long?
    ): ImmutableList<UpdatedKanbanStory> {
        TODO("Not yet implemented")
    }
}
