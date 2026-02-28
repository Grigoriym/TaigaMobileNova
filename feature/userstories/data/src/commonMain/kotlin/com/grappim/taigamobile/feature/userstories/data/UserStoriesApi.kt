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
            url {
                if (params.project != null) parameters.append("project", params.project.toString())
                if (params.sprint != null) parameters.append("milestone", params.sprint.toString())
                if (params.status != null) parameters.append("status", params.status.toString())
                if (params.epic != null) parameters.append("epic", params.epic.toString())
                if (params.page != null) parameters.append("page", params.page.toString())
                if (params.assignedId != null) parameters.append("assigned_users", params.assignedId.toString())
                if (params.isClosed != null) parameters.append("status__is_closed", params.isClosed.toString())
                if (params.watcherId != null) parameters.append("watchers", params.watcherId.toString())
                if (params.isDashboard != null) parameters.append("dashboard", params.isDashboard.toString())
                if (params.query != null) parameters.append("q", params.query)
                parameters.append("page_size", params.pageSize.toString())
                if (params.assignedIds != null) parameters.append("assigned_to", params.assignedIds)
                if (params.epics != null) parameters.append("epic", params.epics)
                if (params.ownerIds != null) parameters.append("owner", params.ownerIds)
                if (params.roles != null) parameters.append("role", params.roles)
                if (params.statuses != null) parameters.append("status", params.statuses)
                if (params.tags != null) parameters.append("tags", params.tags)
            }
            // here and below instead of setting header to "false" remove it,
            // because api always returns unpaginated result if header persists, regardless of its value
            if (params.page == null) headers.append("x-disable-pagination", "true")
        }.body()
}
