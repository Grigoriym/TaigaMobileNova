package com.grappim.taigamobile.testing.api

import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.dto.AttachmentDTO
import com.grappim.taigamobile.feature.workitem.dto.CreateWorkItemRequestDTO
import com.grappim.taigamobile.feature.workitem.dto.PromoteToUserStoryRequestDTO
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.customattribute.CustomAttributeResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.customattribute.CustomAttributesValuesResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.wiki.WikiPageDTO
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.JsonObject

data class GetWorkItemsApiCall(
    val taskPath: String,
    val project: Long?,
    val assignedId: Long?,
    val isClosed: Boolean?,
    val watcherId: Long?,
    val isDashboard: Boolean? = null,
    val isBlocked: Boolean? = null,
    val modifiedDateGte: String? = null,
    val finishDateGte: String? = null,
    val sprint: Long? = null,
    val pageSize: Int? = null,
)

data class PatchWorkItemApiCall(val taskPath: String, val id: Long, val payload: JsonObject)

data class PatchCustomAttributesValuesApiCall(
    val taskPath: String,
    val taskId: Long,
    val payload: JsonObject,
)

data class UploadAttachmentApiCall(
    val taskPath: String,
    val fileName: String,
    val fileBytes: ByteArray,
    val projectId: Long,
    val objectId: Long,
)

data class PatchWikiPageApiCall(val pageId: Long, val payload: JsonObject)

data class CreateWorkItemApiCall(
    val taskPath: String,
    val createRequest: CreateWorkItemRequestDTO,
)

data class PromoteToUserStoryApiCall(
    val taskPath: String,
    val workItemId: Long,
    val body: PromoteToUserStoryRequestDTO,
)

data class GetWorkItemByRefApiCall(val taskPath: String, val project: Long, val ref: Long)

class FakeWorkItemApi : WorkItemApi {

    var workItemByIdResponse: WorkItemResponseDTO? = null
    var workItemsResponse: List<WorkItemResponseDTO> = emptyList()
    val getWorkItemsCalls = mutableListOf<GetWorkItemsApiCall>()
    var getWorkItemsLambda: ((taskPath: String, sprint: Long?, userStory: Any?) -> List<WorkItemResponseDTO>)? = null

    var patchWorkItemResponse: WorkItemResponseDTO? = null
    val patchWorkItemCalls = mutableListOf<PatchWorkItemApiCall>()

    var patchCustomAttributesValuesResponse: CustomAttributesValuesResponseDTO? = null
    val patchCustomAttributesValuesCalls = mutableListOf<PatchCustomAttributesValuesApiCall>()

    var uploadAttachmentResponse: AttachmentDTO? = null
    val uploadAttachmentCalls = mutableListOf<UploadAttachmentApiCall>()

    val deleteAttachmentCalls = mutableListOf<Pair<String, Long>>()

    val watchWorkItemCalls = mutableListOf<Pair<String, Long>>()
    val unwatchWorkItemCalls = mutableListOf<Pair<String, Long>>()

    var customAttributesResponse: List<CustomAttributeResponseDTO> = emptyList()
    var customAttributesValuesResponse: CustomAttributesValuesResponseDTO? = null

    var getAttachmentsResponse: List<AttachmentDTO> = emptyList()

    var patchWikiPageResponse: WikiPageDTO? = null
    val patchWikiPageCalls = mutableListOf<PatchWikiPageApiCall>()

    var createWorkItemResponse: WorkItemResponseDTO? = null
    val createWorkItemCalls = mutableListOf<CreateWorkItemApiCall>()

    var promoteToUserStoryResponse: List<Long> = emptyList()
    val promoteToUserStoryCalls = mutableListOf<PromoteToUserStoryApiCall>()

    var workItemByRefResponse: WorkItemResponseDTO? = null
    val workItemByRefCalls = mutableListOf<GetWorkItemByRefApiCall>()

    val deleteWorkItemCalls = mutableListOf<Pair<String, Long>>()

    override suspend fun getWorkItemById(taskPath: String, id: Long): WorkItemResponseDTO =
        workItemByIdResponse ?: error("workItemByIdResponse not set")

    override suspend fun getWorkItems(
        taskPath: String,
        project: Long?,
        assignedId: Long?,
        assignedIds: String?,
        watcherId: Long?,
        isClosed: Boolean?,
        sprint: Long?,
        userStory: Any?,
        isDashboard: Boolean?,
        isBlocked: Boolean?,
        modifiedDateGte: String?,
        finishDateGte: String?,
        pageSize: Int?
    ): List<WorkItemResponseDTO> {
        getWorkItemsCalls += GetWorkItemsApiCall(
            taskPath = taskPath,
            project = project,
            assignedId = assignedId,
            isClosed = isClosed,
            watcherId = watcherId,
            isDashboard = isDashboard,
            isBlocked = isBlocked,
            modifiedDateGte = modifiedDateGte,
            finishDateGte = finishDateGte,
            sprint = sprint,
            pageSize = pageSize,
        )
        return getWorkItemsLambda?.invoke(taskPath, sprint, userStory) ?: workItemsResponse
    }

    override suspend fun getWorkItemsPagination(
        taskPath: String,
        page: Int?,
        pageSize: Int,
        query: String?,
        project: Long?,
        sprint: Any?,
        assignedIds: String?,
        ownerIds: String?,
        roles: String?,
        statuses: String?,
        epics: String?,
        tags: String?,
        priorities: String?,
        severities: String?,
        types: String?
    ): HttpResponse = error("not used in this test")

    override suspend fun createWorkItem(
        taskPath: String,
        createRequest: CreateWorkItemRequestDTO
    ): WorkItemResponseDTO {
        createWorkItemCalls += CreateWorkItemApiCall(taskPath, createRequest)
        return createWorkItemResponse ?: error("createWorkItemResponse not set")
    }

    override suspend fun getWorkItemByRef(
        taskPath: String,
        project: Long,
        ref: Long
    ): WorkItemResponseDTO {
        workItemByRefCalls += GetWorkItemByRefApiCall(taskPath, project, ref)
        return workItemByRefResponse ?: error("workItemByRefResponse not set")
    }

    override suspend fun patchWorkItem(
        taskPath: String,
        id: Long,
        payload: JsonObject
    ): WorkItemResponseDTO {
        patchWorkItemCalls += PatchWorkItemApiCall(taskPath, id, payload)
        return patchWorkItemResponse ?: error("patchWorkItemResponse not set")
    }

    override suspend fun unwatchWorkItem(taskPath: String, workItemId: Long) {
        unwatchWorkItemCalls += taskPath to workItemId
    }

    override suspend fun watchWorkItem(taskPath: String, workItemId: Long) {
        watchWorkItemCalls += taskPath to workItemId
    }

    override suspend fun deleteWorkItem(taskPath: String, workItemId: Long) {
        deleteWorkItemCalls += taskPath to workItemId
    }

    override suspend fun promoteToUserStory(
        taskPath: String,
        workItemId: Long,
        body: PromoteToUserStoryRequestDTO
    ): List<Long> {
        promoteToUserStoryCalls += PromoteToUserStoryApiCall(taskPath, workItemId, body)
        return promoteToUserStoryResponse
    }

    override suspend fun getAttachments(
        taskPath: String,
        objectId: Long,
        projectId: Long
    ): List<AttachmentDTO> = getAttachmentsResponse

    override suspend fun deleteAttachment(taskPath: String, attachmentId: Long) {
        deleteAttachmentCalls += taskPath to attachmentId
    }

    override suspend fun uploadCommonTaskAttachment(
        taskPath: String,
        fileName: String,
        fileBytes: ByteArray,
        projectId: Long,
        objectId: Long
    ): AttachmentDTO {
        uploadAttachmentCalls += UploadAttachmentApiCall(taskPath, fileName, fileBytes, projectId, objectId)
        return uploadAttachmentResponse ?: error("uploadAttachmentResponse not set")
    }

    override suspend fun getCustomAttributes(
        taskPath: String,
        projectId: Long
    ): List<CustomAttributeResponseDTO> = customAttributesResponse

    override suspend fun getCustomAttributesValues(
        taskPath: String,
        id: Long
    ): CustomAttributesValuesResponseDTO =
        customAttributesValuesResponse ?: error("customAttributesValuesResponse not set")

    override suspend fun patchWikiPage(pageId: Long, payload: JsonObject): WikiPageDTO {
        patchWikiPageCalls += PatchWikiPageApiCall(pageId, payload)
        return patchWikiPageResponse ?: error("patchWikiPageResponse not set")
    }

    override suspend fun patchCustomAttributesValues(
        taskPath: String,
        taskId: Long,
        payload: JsonObject
    ): CustomAttributesValuesResponseDTO {
        patchCustomAttributesValuesCalls += PatchCustomAttributesValuesApiCall(taskPath, taskId, payload)
        return patchCustomAttributesValuesResponse ?: error("patchCustomAttributesValuesResponse not set")
    }
}
