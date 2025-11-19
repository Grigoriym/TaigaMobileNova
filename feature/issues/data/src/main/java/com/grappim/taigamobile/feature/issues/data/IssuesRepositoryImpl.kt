package com.grappim.taigamobile.feature.issues.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.api.AttachmentMapper
import com.grappim.taigamobile.core.api.CommonTaskMapper
import com.grappim.taigamobile.core.api.CustomFieldsMapper
import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.CustomFields
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.domain.patch.PatchedCustomAttributes
import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.issues.domain.IssueTask
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import com.grappim.taigamobile.feature.workitem.data.PatchedDataMapper
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathSingular
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class IssuesRepositoryImpl @Inject constructor(
    private val issuesApi: IssuesApi,
    private val taigaStorage: TaigaStorage,
    private val commonTaskMapper: CommonTaskMapper,
    private val issueTaskMapper: IssueTaskMapper,
    private val attachmentMapper: AttachmentMapper,
    private val customFieldsMapper: CustomFieldsMapper,
    private val patchedDataMapper: PatchedDataMapper,
    private val workItemApi: WorkItemApi,
    private val workItemRepository: WorkItemRepository
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

    override suspend fun watchIssue(issueId: Long) {
        workItemApi.watchWorkItem(
            workItemId = issueId,
            taskPath = WorkItemPathPlural(CommonTaskType.Issue)
        )
    }

    override suspend fun unwatchIssue(issueId: Long) {
        workItemApi.unwatchWorkItem(
            workItemId = issueId,
            taskPath = WorkItemPathPlural(CommonTaskType.Issue)
        )
    }

    override suspend fun deleteIssue(id: Long) {
        workItemApi.deleteWorkItem(
            workItemId = id,
            taskPath = WorkItemPathPlural(CommonTaskType.Issue)
        )
    }

    override suspend fun deleteAttachment(attachment: Attachment) {
        workItemApi.deleteAttachment(
            attachmentId = attachment.id,
            taskPath = WorkItemPathPlural(CommonTaskType.Issue)
        )
    }

    override suspend fun addAttachment(
        issueId: Long,
        fileName: String,
        fileByteArray: ByteArray
    ): Attachment {
        return workItemRepository.addAttachment(
            workItemId = issueId,
            fileName = fileName,
            fileByteArray = fileByteArray,
            projectId = taigaStorage.currentProjectIdFlow.first(),
            commonTaskType = CommonTaskType.Issue
        )
    }

    override suspend fun patchData(
        version: Long,
        issueId: Long,
        payload: ImmutableMap<String, Any?>
    ): PatchedData {
        return workItemRepository.patchData(
            commonTaskType = CommonTaskType.Issue,
            workItemId = issueId,
            payload = payload,
            version = version
        )
    }

    override suspend fun patchCustomAttributes(
        version: Long,
        issueId: Long,
        payload: ImmutableMap<String, Any?>
    ): PatchedCustomAttributes {
        val editedMap = payload.toPersistentMap().put("version", version)
        val result = workItemApi.patchCustomAttributesValues(
            taskPath = WorkItemPathPlural(CommonTaskType.Issue),
            taskId = issueId,
            payload = editedMap
        )

        return patchedDataMapper.toDomainCustomAttrs(result)
    }

    override suspend fun getIssue(id: Long, filtersData: FiltersData): IssueTask {
        val response = workItemApi.getWorkItemById(
            taskPath = WorkItemPathPlural(CommonTaskType.Issue),
            id = id
        )
        return issueTaskMapper.toDomain(resp = response, filters = filtersData)
    }

    override suspend fun getCustomFields(id: Long): CustomFields = coroutineScope {
        val attributes = async {
            workItemApi.getCustomAttributes(
                taskPath = WorkItemPathSingular(CommonTaskType.Issue),
                projectId = taigaStorage.currentProjectIdFlow.first()
            )
        }
        val values = async {
            workItemApi.getCustomAttributesValues(
                id = id,
                taskPath = WorkItemPathPlural(CommonTaskType.Issue),
            )
        }

        customFieldsMapper.toDomain(
            attributes = attributes.await(),
            values = values.await()
        )
    }

    override suspend fun getIssueAttachments(taskId: Long): List<Attachment> {
        val projectId = taigaStorage.currentProjectIdFlow.first()

        return workItemApi.getAttachments(
            taskPath = WorkItemPathPlural(CommonTaskType.Issue),
            objectId = taskId,
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
