package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.feature.workitem.dto.AttachmentDTO
import com.grappim.taigamobile.feature.workitem.dto.CreateWorkItemRequestDTO
import com.grappim.taigamobile.feature.workitem.dto.PromoteToUserStoryRequestDTO
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.customattribute.CustomAttributeResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.customattribute.CustomAttributesValuesResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.wiki.WikiPageDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.discardRemaining
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.JsonObject
import org.koin.core.annotation.Single

@Single
class WorkItemApi(private val httpClient: HttpClient) {

    // Work Item
    suspend fun getWorkItemById(taskPath: String, id: Long): WorkItemResponseDTO =
        httpClient.get("$taskPath/$id").body()

    suspend fun getWorkItems(
        taskPath: String,
        project: Long? = null,
        assignedId: Long? = null,
        assignedIds: String? = null,
        watcherId: Long? = null,
        isClosed: Boolean? = null,
        sprint: Long? = null,
        userStory: Any? = null,
        isDashboard: Boolean? = null,
        isBlocked: Boolean? = null,
        modifiedDateGte: String? = null,
        finishDateGte: String? = null,
        pageSize: Int? = null
    ): List<WorkItemResponseDTO> = httpClient.get(taskPath) {
        url {
            if (project != null) parameters.append("project", project.toString())
            if (assignedId != null) parameters.append("assigned_to", assignedId.toString())
            if (assignedIds != null) parameters.append("assigned_to", assignedIds)
            if (watcherId != null) parameters.append("watchers", watcherId.toString())
            if (isClosed != null) parameters.append("status__is_closed", isClosed.toString())
            if (sprint != null) parameters.append("milestone", sprint.toString())
            if (userStory != null) parameters.append("user_story", userStory.toString())
            if (isDashboard != null) parameters.append("dashboard", isDashboard.toString())
            if (isBlocked != null) parameters.append("is_blocked", isBlocked.toString())
            if (modifiedDateGte != null) parameters.append("modified_date__gte", modifiedDateGte)
            if (finishDateGte != null) parameters.append("finish_date__gte", finishDateGte)
            if (pageSize != null) parameters.append("page_size", pageSize.toString())
        }
    }.body()

    suspend fun getWorkItemsPagination(
        taskPath: String,
        page: Int? = null,
        pageSize: Int = 20,
        query: String? = null,
        project: Long? = null,
        sprint: Any? = null,
        assignedIds: String? = null,
        ownerIds: String? = null,
        roles: String? = null,
        statuses: String? = null,
        epics: String? = null,
        tags: String? = null,
        priorities: String? = null,
        severities: String? = null,
        types: String? = null
    ): HttpResponse = httpClient.get(taskPath) {
        url {
            if (page != null) parameters.append("page", page.toString())
            parameters.append("page_size", pageSize.toString())
            if (query != null) parameters.append("q", query)
            if (project != null) parameters.append("project", project.toString())
            if (sprint != null) parameters.append("milestone", sprint.toString())
            if (assignedIds != null) parameters.append("assigned_to", assignedIds)
            if (ownerIds != null) parameters.append("owner", ownerIds)
            if (roles != null) parameters.append("role", roles)
            if (statuses != null) parameters.append("status", statuses)
            if (epics != null) parameters.append("epic", epics)
            if (tags != null) parameters.append("tags", tags)
            if (priorities != null) parameters.append("priority", priorities)
            if (severities != null) parameters.append("severity", severities)
            if (types != null) parameters.append("type", types)
        }
        if (page == null) headers.append("x-disable-pagination", "true")
    }

    suspend fun createWorkItem(taskPath: String, createRequest: CreateWorkItemRequestDTO): WorkItemResponseDTO =
        httpClient.post(taskPath) {
            setBody(createRequest)
        }.body()

    suspend fun getWorkItemByRef(taskPath: String, project: Long, ref: Long): WorkItemResponseDTO =
        httpClient.get("$taskPath/by_ref") {
            url {
                parameters.append("project", project.toString())
                parameters.append("ref", ref.toString())
            }
        }.body()

    suspend fun patchWorkItem(taskPath: String, id: Long, payload: JsonObject): WorkItemResponseDTO =
        httpClient.patch("$taskPath/$id") {
            setBody(payload)
        }.body()

    suspend fun unwatchWorkItem(taskPath: String, workItemId: Long) {
        httpClient.post("$taskPath/$workItemId/unwatch").discardRemaining()
    }

    suspend fun watchWorkItem(taskPath: String, workItemId: Long) {
        httpClient.post("$taskPath/$workItemId/watch").discardRemaining()
    }

    suspend fun deleteWorkItem(taskPath: String, workItemId: Long) {
        httpClient.delete("$taskPath/$workItemId").discardRemaining()
    }

    suspend fun promoteToUserStory(taskPath: String, workItemId: Long, body: PromoteToUserStoryRequestDTO): List<Long> =
        httpClient.post("$taskPath/$workItemId/promote_to_user_story") {
            setBody(body)
        }.body()

    // Attachments

    suspend fun getAttachments(taskPath: String, objectId: Long, projectId: Long): List<AttachmentDTO> =
        httpClient.get("$taskPath/attachments") {
            url {
                parameters.append("object_id", objectId.toString())
                parameters.append("project", projectId.toString())
            }
        }.body()

    suspend fun deleteAttachment(taskPath: String, attachmentId: Long) {
        httpClient.delete("$taskPath/attachments/$attachmentId").discardRemaining()
    }

    suspend fun uploadCommonTaskAttachment(
        taskPath: String,
        fileName: String,
        fileBytes: ByteArray,
        projectId: Long,
        objectId: Long
    ): AttachmentDTO = httpClient.post("$taskPath/attachments") {
        setBody(
            MultiPartFormDataContent(
                formData {
                    append(
                        "attached_file",
                        fileBytes,
                        Headers.build {
                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                        }
                    )
                    append("project", projectId.toString())
                    append("object_id", objectId.toString())
                }
            )
        )
    }.body()

    // Custom Attributes

    suspend fun getCustomAttributes(taskPath: String, projectId: Long): List<CustomAttributeResponseDTO> =
        httpClient.get("$taskPath-custom-attributes") {
            url {
                parameters.append("project", projectId.toString())
            }
        }.body()

    suspend fun getCustomAttributesValues(taskPath: String, id: Long): CustomAttributesValuesResponseDTO =
        httpClient.get("$taskPath/custom-attributes-values/$id").body()

    // Wiki

    suspend fun patchWikiPage(pageId: Long, payload: JsonObject): WikiPageDTO = httpClient.patch("wiki/$pageId") {
        setBody(payload)
    }.body()

    suspend fun patchCustomAttributesValues(
        taskPath: String,
        taskId: Long,
        payload: JsonObject
    ): CustomAttributesValuesResponseDTO = httpClient.patch("$taskPath/custom-attributes-values/$taskId") {
        setBody(payload)
    }.body()
}
