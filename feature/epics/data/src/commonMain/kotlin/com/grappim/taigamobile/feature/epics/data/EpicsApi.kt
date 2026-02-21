package com.grappim.taigamobile.feature.epics.data

import com.grappim.taigamobile.feature.epics.dto.LinkToEpicRequestDTO
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.discardRemaining
import org.koin.core.annotation.Single

@Single
class EpicsApi(private val httpClient: HttpClient) {

    suspend fun getEpics(
        page: Int? = null,
        pageSize: Int = 20,
        project: Long? = null,
        query: String? = null,
        assignedId: Long? = null,
        isClosed: Boolean? = null,
        watcherId: Long? = null,
        assignedIds: String? = null,
        ownerIds: String? = null,
        statuses: String? = null,
        tags: String? = null
    ): List<WorkItemResponseDTO> = httpClient.get("epics") {
        url {
            if (page != null) parameters.append("page", page.toString())
            parameters.append("page_size", pageSize.toString())
            if (project != null) parameters.append("project", project.toString())
            if (query != null) parameters.append("q", query)
            if (assignedId != null) parameters.append("assigned_to", assignedId.toString())
            if (isClosed != null) parameters.append("status__is_closed", isClosed.toString())
            if (watcherId != null) parameters.append("watchers", watcherId.toString())
            if (assignedIds != null) parameters.append("assigned_to", assignedIds)
            if (ownerIds != null) parameters.append("owner", ownerIds)
            if (statuses != null) parameters.append("status", statuses)
            if (tags != null) parameters.append("tags", tags)
        }
        if (page == null) headers.append("x-disable-pagination", "true")
    }.body()

    suspend fun linkToEpic(epicId: Long, linkToEpicRequest: LinkToEpicRequestDTO) {
        httpClient.post("epics/$epicId/related_userstories") {
            setBody(linkToEpicRequest)
        }.discardRemaining()
    }

    suspend fun unlinkFromEpic(epicId: Long, userStoryId: Long) {
        httpClient.delete("epics/$epicId/related_userstories/$userStoryId").discardRemaining()
    }
}
