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
)

class FakeWorkItemApi : WorkItemApi {

    var workItemByIdResponse: WorkItemResponseDTO? = null
    var workItemsResponse: List<WorkItemResponseDTO> = emptyList()
    val getWorkItemsCalls = mutableListOf<GetWorkItemsApiCall>()
    var getWorkItemsLambda: ((taskPath: String, sprint: Long?, userStory: Any?) -> List<WorkItemResponseDTO>)? = null

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
    ): WorkItemResponseDTO = error("not used in this test")

    override suspend fun getWorkItemByRef(
        taskPath: String,
        project: Long,
        ref: Long
    ): WorkItemResponseDTO = error("not used in this test")

    override suspend fun patchWorkItem(
        taskPath: String,
        id: Long,
        payload: JsonObject
    ): WorkItemResponseDTO = error("not used in this test")

    override suspend fun unwatchWorkItem(taskPath: String, workItemId: Long): Unit =
        error("not used in this test")

    override suspend fun watchWorkItem(taskPath: String, workItemId: Long): Unit =
        error("not used in this test")

    override suspend fun deleteWorkItem(taskPath: String, workItemId: Long): Unit =
        error("not used in this test")

    override suspend fun promoteToUserStory(
        taskPath: String,
        workItemId: Long,
        body: PromoteToUserStoryRequestDTO
    ): List<Long> = error("not used in this test")

    override suspend fun getAttachments(
        taskPath: String,
        objectId: Long,
        projectId: Long
    ): List<AttachmentDTO> = error("not used in this test")

    override suspend fun deleteAttachment(taskPath: String, attachmentId: Long): Unit =
        error("not used in this test")

    override suspend fun uploadCommonTaskAttachment(
        taskPath: String,
        fileName: String,
        fileBytes: ByteArray,
        projectId: Long,
        objectId: Long
    ): AttachmentDTO = error("not used in this test")

    override suspend fun getCustomAttributes(
        taskPath: String,
        projectId: Long
    ): List<CustomAttributeResponseDTO> = error("not used in this test")

    override suspend fun getCustomAttributesValues(
        taskPath: String,
        id: Long
    ): CustomAttributesValuesResponseDTO = error("not used in this test")

    override suspend fun patchWikiPage(pageId: Long, payload: JsonObject): WikiPageDTO =
        error("not used in this test")

    override suspend fun patchCustomAttributesValues(
        taskPath: String,
        taskId: Long,
        payload: JsonObject
    ): CustomAttributesValuesResponseDTO = error("not used in this test")
}
