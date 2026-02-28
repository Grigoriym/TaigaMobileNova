package com.grappim.taigamobile.feature.userstories.data

import com.grappim.taigamobile.feature.userstories.dto.BulkUpdateKanbanOrderRequest
import com.grappim.taigamobile.feature.userstories.dto.BulkUpdateKanbanOrderResponseItem
import com.grappim.taigamobile.feature.userstories.dto.CreateUserStoryRequest
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.URLBuilder
import org.koin.core.annotation.Single

interface UserStoriesApi {
    suspend fun createUserStory(createUserStoryRequest: CreateUserStoryRequest): WorkItemResponseDTO
    suspend fun bulkUpdateKanbanOrder(request: BulkUpdateKanbanOrderRequest): List<BulkUpdateKanbanOrderResponseItem>
    suspend fun getUserStories(params: GetUserStoriesParams = GetUserStoriesParams()): List<WorkItemResponseDTO>
}

@Single(binds = [UserStoriesApi::class])
class UserStoriesApiImpl(private val httpClient: HttpClient) : UserStoriesApi {

    override suspend fun createUserStory(createUserStoryRequest: CreateUserStoryRequest): WorkItemResponseDTO =
        httpClient.post("userstories") {
            setBody(createUserStoryRequest)
        }.body()

    override suspend fun bulkUpdateKanbanOrder(
        request: BulkUpdateKanbanOrderRequest
    ): List<BulkUpdateKanbanOrderResponseItem> = httpClient.post("userstories/bulk_update_kanban_order") {
        setBody(request)
    }.body()

    override suspend fun getUserStories(params: GetUserStoriesParams): List<WorkItemResponseDTO> =
        httpClient.get("userstories") {
            url { applyUserStoryParams(params) }
            // here and below instead of setting header to "false" remove it,
            // because api always returns unpaginated result if header persists, regardless of its value
            if (params.page == null) headers.append("x-disable-pagination", "true")
        }.body()

    private fun URLBuilder.applyUserStoryParams(params: GetUserStoriesParams) {
        listOf(
            "project" to params.project,
            "milestone" to params.sprint,
            "status" to params.status,
            "epic" to params.epic,
            "page" to params.page,
            "assigned_users" to params.assignedId,
            "status__is_closed" to params.isClosed,
            "watchers" to params.watcherId,
            "dashboard" to params.isDashboard,
            "q" to params.query,
            "assigned_to" to params.assignedIds,
            "epic" to params.epics,
            "owner" to params.ownerIds,
            "role" to params.roles,
            "status" to params.statuses,
            "tags" to params.tags
        ).forEach { (key, value) ->
            if (value != null) parameters.append(key, value.toString())
        }
        parameters.append("page_size", params.pageSize.toString())
    }
}
