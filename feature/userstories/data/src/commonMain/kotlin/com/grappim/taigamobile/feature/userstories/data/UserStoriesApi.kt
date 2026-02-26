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
    suspend fun getUserStories(
        project: Long? = null,
        sprint: Any? = null,
        status: Long? = null,
        epic: Long? = null,
        page: Int? = null,
        assignedId: Long? = null,
        isClosed: Boolean? = null,
        watcherId: Long? = null,
        isDashboard: Boolean? = null,
        query: String? = null,
        pageSize: Int = 20,
        assignedIds: String? = null,
        epics: String? = null,
        ownerIds: String? = null,
        roles: String? = null,
        statuses: String? = null,
        tags: String? = null
    ): List<WorkItemResponseDTO>
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
    ): List<WorkItemResponseDTO> = httpClient.get("userstories") {
        url {
            if (project != null) parameters.append("project", project.toString())
            if (sprint != null) parameters.append("milestone", sprint.toString())
            if (status != null) parameters.append("status", status.toString())
            if (epic != null) parameters.append("epic", epic.toString())
            if (page != null) parameters.append("page", page.toString())
            if (assignedId != null) parameters.append("assigned_users", assignedId.toString())
            if (isClosed != null) parameters.append("status__is_closed", isClosed.toString())
            if (watcherId != null) parameters.append("watchers", watcherId.toString())
            if (isDashboard != null) parameters.append("dashboard", isDashboard.toString())
            if (query != null) parameters.append("q", query)
            parameters.append("page_size", pageSize.toString())
            if (assignedIds != null) parameters.append("assigned_to", assignedIds)
            if (epics != null) parameters.append("epic", epics)
            if (ownerIds != null) parameters.append("owner", ownerIds)
            if (roles != null) parameters.append("role", roles)
            if (statuses != null) parameters.append("status", statuses)
            if (tags != null) parameters.append("tags", tags)
        }
        // here and below instead of setting header to "false" remove it,
        // because api always returns unpaginated result if header persists, regardless of its value
        if (page == null) headers.append("x-disable-pagination", "true")
    }.body()
}
