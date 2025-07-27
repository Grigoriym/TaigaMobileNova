package com.grappim.taigamobile.feature.issues.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.api.AttachmentMapper
import com.grappim.taigamobile.core.api.CommonTaskMapper
import com.grappim.taigamobile.core.api.CustomFieldsMapper
import com.grappim.taigamobile.core.api.PatchedDataMapper
import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.CustomFields
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.domain.patch.PatchedCustomAttributes
import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.issues.domain.IssueTask
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import kotlinx.collections.immutable.PersistentMap
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class IssuesRepositoryImpl @Inject constructor(
    private val issuesApi: IssuesApi,
    private val taigaStorage: TaigaStorage,
    private val commonTaskMapper: CommonTaskMapper,
    private val issueTaskMapper: IssueTaskMapper,
    private val attachmentMapper: AttachmentMapper,
    private val serverStorage: ServerStorage,
    private val customFieldsMapper: CustomFieldsMapper,
    private val patchedDataMapper: PatchedDataMapper
) : IssuesRepository {
    private var issuesPagingSource: IssuesPagingSource? = null

    override fun getIssuesPaging(filtersDataDTO: FiltersDataDTO): Flow<PagingData<CommonTask>> =
        Pager(
            PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            )
        ) {
            IssuesPagingSource(issuesApi, filtersDataDTO, taigaStorage).also {
                issuesPagingSource = it
            }
        }.flow

    override fun refreshIssues() {
        issuesPagingSource?.invalidate()
    }

    override suspend fun deleteComment(issueId: Long, commentId: String) {
        issuesApi.deleteComment(issueId = issueId, commentId = commentId)
    }

    override suspend fun deleteIssue(id: Long) {
        issuesApi.deleteCommonTask(id)
    }

    override suspend fun deleteAttachment(attachment: Attachment) {
        issuesApi.deleteAttachment(attachment.id)
    }

    override suspend fun addAttachment(
        issueId: Long,
        fileName: String,
        fileByteArray: ByteArray
    ): Attachment {
        val file = MultipartBody.Part.createFormData(
            name = "attached_file",
            filename = fileName,
            body = fileByteArray.toRequestBody("*/*".toMediaType())
        )
        val project = MultipartBody.Part.createFormData(
            "project",
            taigaStorage.currentProjectIdFlow.first().toString()
        )
        val objectId = MultipartBody.Part.createFormData("object_id", issueId.toString())

        val dto = issuesApi.uploadCommonTaskAttachment(
            file = file,
            project = project,
            objectId = objectId
        )
        return attachmentMapper.toDomain(dto)
    }

    override suspend fun patchData(
        version: Long,
        issueId: Long,
        payload: PersistentMap<String, Any?>
    ): PatchedData {
        val editedMap = payload.put("version", version)
        val result = issuesApi.patchIssue(issueId = issueId, payload = editedMap)
        return patchedDataMapper.toDomain(result)
    }

    override suspend fun patchCustomAttributes(
        version: Long,
        issueId: Long,
        payload: PersistentMap<String, Any?>
    ): PatchedCustomAttributes {
        val editedMap = payload.put("version", version)
        val result = issuesApi.patchCustomAttributesValues(taskId = issueId, payload = editedMap)
        return patchedDataMapper.toDomainCustomAttrs(result)
    }

    override suspend fun getIssueByRef(ref: Int, filtersData: FiltersData): IssueTask =
        coroutineScope {
            val projectId = taigaStorage.currentProjectIdFlow.first()
            val resp = issuesApi.getIssueByRef(projectId, ref)

            issueTaskMapper.toDomain(
                resp = resp,
                server = serverStorage.server,
                filters = filtersData
            )
        }

    override suspend fun getCustomFields(commonTaskId: Long): CustomFields = coroutineScope {
        val attributes = async {
            issuesApi.getIssueCustomAttributes(
                projectId = taigaStorage.currentProjectIdFlow.first()
            )
        }
        val values = async {
            issuesApi.getIssueCustomAttributesValues(
                taskId = commonTaskId
            )
        }

        customFieldsMapper.toDomain(
            attributes = attributes.await(),
            values = values.await()
        )
    }

    override suspend fun getIssueAttachments(taskId: Long): List<Attachment> {
        val projectId = taigaStorage.currentProjectIdFlow.first()

        return issuesApi.getIssueAttachments(
            storyId = taskId,
            projectId = projectId
        ).map { attachmentDTO ->
            attachmentMapper.toDomain(attachmentDTO)
        }
    }

    override suspend fun createIssue(
        title: String,
        description: String,
        sprintId: Long?
    ): CommonTaskResponse = issuesApi.createIssue(
        createIssueRequest = CreateIssueRequest(
            project = taigaStorage.currentProjectIdFlow.first(),
            subject = title,
            description = description,
            milestone = sprintId
        )
    )

    override suspend fun getIssues(
        isClosed: Boolean,
        assignedIds: String?,
        watcherId: Long?,
        project: Long?,
        sprint: Long?
    ): List<CommonTask> = issuesApi.getIssues(
        assignedIds = assignedIds,
        isClosed = isClosed,
        watcherId = watcherId,
        project = project,
        sprint = sprint
    ).map { commonTaskMapper.toDomain(it, CommonTaskType.Issue) }
}
