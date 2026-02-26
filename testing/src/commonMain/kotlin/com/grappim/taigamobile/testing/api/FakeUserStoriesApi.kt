package com.grappim.taigamobile.testing.api

import com.grappim.taigamobile.feature.userstories.data.UserStoriesApi
import com.grappim.taigamobile.feature.userstories.dto.BulkUpdateKanbanOrderRequest
import com.grappim.taigamobile.feature.userstories.dto.BulkUpdateKanbanOrderResponseItem
import com.grappim.taigamobile.feature.userstories.dto.CreateUserStoryRequest
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO

class FakeUserStoriesApi : UserStoriesApi {

    var getUserStoriesResult: List<WorkItemResponseDTO> = emptyList()

    var createUserStoryResult: WorkItemResponseDTO? = null
    val createUserStoryCalls = mutableListOf<CreateUserStoryRequest>()

    override suspend fun createUserStory(createUserStoryRequest: CreateUserStoryRequest): WorkItemResponseDTO {
        createUserStoryCalls += createUserStoryRequest
        return createUserStoryResult ?: error("createUserStoryResult not set")
    }

    override suspend fun bulkUpdateKanbanOrder(request: BulkUpdateKanbanOrderRequest): List<BulkUpdateKanbanOrderResponseItem> =
        error("not used in this test")

    override suspend fun getUserStories(
        project: Long?,
        sprint: Any?,
        status: Long?,
        epic: Long?,
        page: Int?,
        assignedId: Long?,
        isClosed: Boolean?,
        watcherId: Long?,
        isDashboard: Boolean?,
        query: String?,
        pageSize: Int,
        assignedIds: String?,
        epics: String?,
        ownerIds: String?,
        roles: String?,
        statuses: String?,
        tags: String?
    ): List<WorkItemResponseDTO> = getUserStoriesResult
}
