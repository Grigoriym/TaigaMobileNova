package com.grappim.taigamobile.feature.issues.data

import com.grappim.taigamobile.feature.issues.dto.CreateIssueRequestDTO
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.koin.core.annotation.Single

@Single
class IssuesApi(private val httpClient: HttpClient) {

    suspend fun getIssues(
        page: Int? = null,
        project: Long? = null,
        query: String? = null,
        sprint: Long? = null,
        isClosed: Boolean? = null,
        watcherId: Long? = null,
        pageSize: Int = 20,
        assignedIds: String? = null,
        ownerIds: String? = null,
        priorities: String? = null,
        severities: String? = null,
        types: String? = null,
        roles: String? = null,
        statuses: String? = null,
        tags: String? = null
    ): List<WorkItemResponseDTO> = httpClient.get("issues") {
        url {
            if (page != null) parameters.append("page", page.toString())
            if (project != null) parameters.append("project", project.toString())
            if (query != null) parameters.append("q", query)
            if (sprint != null) parameters.append("milestone", sprint.toString())
            if (isClosed != null) parameters.append("status__is_closed", isClosed.toString())
            if (watcherId != null) parameters.append("watchers", watcherId.toString())
            parameters.append("page_size", pageSize.toString())
            if (assignedIds != null) parameters.append("assigned_to", assignedIds)
            if (ownerIds != null) parameters.append("owner", ownerIds)
            if (priorities != null) parameters.append("priority", priorities)
            if (severities != null) parameters.append("severity", severities)
            if (types != null) parameters.append("type", types)
            if (roles != null) parameters.append("role", roles)
            if (statuses != null) parameters.append("status", statuses)
            if (tags != null) parameters.append("tags", tags)
        }
        if (page == null) headers.append("x-disable-pagination", "true")
    }.body()

    suspend fun createIssue(createIssueRequest: CreateIssueRequestDTO): WorkItemResponseDTO =
        httpClient.post("issues") {
            setBody(createIssueRequest)
        }.body()
}
