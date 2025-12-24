package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.Attachment
import com.grappim.taigamobile.feature.workitem.domain.PatchedCustomAttributes
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.UpdateWorkItem
import com.grappim.taigamobile.feature.workitem.domain.WatchersListUpdateData
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathSingular
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFields
import com.grappim.taigamobile.feature.workitem.domain.getPluralPath
import com.grappim.taigamobile.feature.workitem.dto.CreateWorkItemRequestDTO
import com.grappim.taigamobile.feature.workitem.dto.PromoteToUserStoryRequestDTO
import com.grappim.taigamobile.feature.workitem.mapper.AttachmentMapper
import com.grappim.taigamobile.feature.workitem.mapper.CustomFieldsMapper
import com.grappim.taigamobile.feature.workitem.mapper.PatchedDataMapper
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class WorkItemRepositoryImpl @Inject constructor(
    private val workItemApi: WorkItemApi,
    private val patchedDataMapper: PatchedDataMapper,
    private val attachmentMapper: AttachmentMapper,
    private val workItemMapper: WorkItemMapper,
    private val usersRepository: UsersRepository,
    private val customFieldsMapper: CustomFieldsMapper,
    private val taigaStorage: TaigaStorage
) : WorkItemRepository {

    override suspend fun getWorkItems(
        commonTaskType: CommonTaskType,
        projectId: Long,
        assignedId: Long?,
        isClosed: Boolean?,
        watcherId: Long?,
        isDashboard: Boolean?,
        assignedIds: String?
    ): ImmutableList<WorkItem> {
        val response = workItemApi.getWorkItems(
            taskPath = WorkItemPathPlural(commonTaskType),
            project = projectId,
            assignedId = assignedId,
            isClosed = isClosed,
            watcherId = watcherId,
            isDashboard = isDashboard
        )
        return workItemMapper.toDomainList(response, commonTaskType)
    }

    override suspend fun patchData(
        version: Long,
        workItemId: Long,
        payload: ImmutableMap<String, Any?>,
        commonTaskType: CommonTaskType
    ): PatchedData {
        val editedMap = payload.toPersistentMap().put("version", version)
        val result = workItemApi.patchWorkItem(
            taskPath = WorkItemPathPlural(commonTaskType),
            id = workItemId,
            payload = editedMap
        )
        return patchedDataMapper.toDomain(result)
    }

    override suspend fun patchCustomAttributes(
        customAttributesVersion: Long,
        workItemId: Long,
        payload: ImmutableMap<String, Any?>,
        commonTaskType: CommonTaskType
    ): PatchedCustomAttributes {
        val editedMap = payload.toPersistentMap().put("version", customAttributesVersion)
        val result = workItemApi.patchCustomAttributesValues(
            taskPath = WorkItemPathPlural(commonTaskType),
            taskId = workItemId,
            payload = editedMap
        )
        return patchedDataMapper.toDomainCustomAttrs(result)
    }

    override suspend fun addAttachment(
        workItemId: Long,
        fileName: String,
        fileByteArray: ByteArray,
        projectId: Long,
        taskIdentifier: TaskIdentifier
    ): Attachment {
        val file = MultipartBody.Part.createFormData(
            name = "attached_file",
            filename = fileName,
            body = fileByteArray.toRequestBody("*/*".toMediaType())
        )
        val project = MultipartBody.Part.createFormData(
            "project",
            projectId.toString()
        )
        val objectId = MultipartBody.Part.createFormData("object_id", workItemId.toString())

        val dto = workItemApi.uploadCommonTaskAttachment(
            taskPath = taskIdentifier.getPluralPath(),
            file = file,
            project = project,
            objectId = objectId
        )
        return attachmentMapper.toDomain(dto)
    }

    override suspend fun deleteAttachment(attachment: Attachment, taskIdentifier: TaskIdentifier) {
        workItemApi.deleteAttachment(
            taskPath = taskIdentifier.getPluralPath(),
            attachmentId = attachment.id
        )
    }

    override suspend fun watchWorkItem(workItemId: Long, commonTaskType: CommonTaskType) {
        workItemApi.watchWorkItem(
            taskPath = WorkItemPathPlural(commonTaskType),
            workItemId = workItemId
        )
    }

    override suspend fun unwatchWorkItem(workItemId: Long, commonTaskType: CommonTaskType) {
        workItemApi.unwatchWorkItem(
            taskPath = WorkItemPathPlural(commonTaskType),
            workItemId = workItemId
        )
    }

    override suspend fun getUpdateWorkItem(workItemId: Long, commonTaskType: CommonTaskType): UpdateWorkItem {
        val response = workItemApi.getWorkItemById(
            taskPath = WorkItemPathPlural(commonTaskType),
            id = workItemId
        )
        return workItemMapper.toUpdateDomain(response)
    }

    override suspend fun updateWatchersData(
        version: Long,
        workItemId: Long,
        newWatchers: ImmutableList<Long>,
        commonTaskType: CommonTaskType
    ): WatchersListUpdateData = coroutineScope {
        val payload = mapOf("watchers" to newWatchers).toPersistentMap()
        val patchedData = patchData(
            version = version,
            workItemId = workItemId,
            payload = payload,
            commonTaskType = commonTaskType
        )

        val watchers: ImmutableList<User>
        val isWatchedByMe: Boolean
        if (newWatchers.isEmpty()) {
            watchers = persistentListOf()
            isWatchedByMe = false
        } else {
            watchers = usersRepository.getUsersList(newWatchers.toList()).toPersistentList()
            isWatchedByMe = usersRepository.isAnyAssignedToMe(watchers)
        }

        WatchersListUpdateData(
            version = patchedData.newVersion,
            isWatchedByMe = isWatchedByMe,
            watchers = watchers
        )
    }

    override suspend fun getCustomFields(workItemId: Long, commonTaskType: CommonTaskType): CustomFields =
        coroutineScope {
            val attributes = async {
                workItemApi.getCustomAttributes(
                    taskPath = WorkItemPathSingular(commonTaskType),
                    projectId = taigaStorage.currentProjectIdFlow.first()
                )
            }
            val values = async {
                workItemApi.getCustomAttributesValues(
                    id = workItemId,
                    taskPath = WorkItemPathPlural(commonTaskType)
                )
            }

            customFieldsMapper.toDomain(
                attributes = attributes.await(),
                values = values.await()
            )
        }

    override suspend fun getWorkItemAttachments(
        workItemId: Long,
        taskIdentifier: TaskIdentifier
    ): ImmutableList<Attachment> {
        val projectId = taigaStorage.currentProjectIdFlow.first()

        val attachments = workItemApi.getAttachments(
            taskPath = taskIdentifier.getPluralPath(),
            objectId = workItemId,
            projectId = projectId
        )

        return attachmentMapper.toDomain(attachments)
    }

    override suspend fun deleteWorkItem(workItemId: Long, commonTaskType: CommonTaskType) {
        workItemApi.deleteWorkItem(
            workItemId = workItemId,
            taskPath = WorkItemPathPlural(commonTaskType)
        )
    }

    override suspend fun patchWikiPage(pageId: Long, version: Long, payload: ImmutableMap<String, Any?>): PatchedData {
        val editedMap = payload.toPersistentMap().put("version", version)
        val response = workItemApi.patchWikiPage(
            pageId = pageId,
            payload = editedMap
        )
        return patchedDataMapper.fromWiki(response)
    }

    override suspend fun createWorkItem(
        commonTaskType: CommonTaskType,
        subject: String,
        description: String,
        status: Long?
    ): WorkItem {
        val response = workItemApi.createWorkItem(
            taskPath = WorkItemPathPlural(commonTaskType),
            createRequest = CreateWorkItemRequestDTO(
                project = taigaStorage.currentProjectIdFlow.first(),
                subject = subject,
                description = description,
                status = status
            )
        )
        return workItemMapper.toDomain(response, commonTaskType)
    }

    override suspend fun promoteToUserStory(workItemId: Long, commonTaskType: CommonTaskType): WorkItem {
        if (commonTaskType !in listOf(CommonTaskType.Issue, CommonTaskType.Task)) {
            error("Invalid task type to promote to user story")
        }
        val projectId = taigaStorage.currentProjectIdFlow.first()

        val response = workItemApi.promoteToUserStory(
            taskPath = WorkItemPathPlural(commonTaskType),
            workItemId = workItemId,
            body = PromoteToUserStoryRequestDTO(
                projectId = projectId
            )
        )
        val newUserStoryRef = response.firstOrNull() ?: error("User story ref not found")

        val userStory = workItemApi.getWorkItemByRef(
            taskPath = WorkItemPathPlural(CommonTaskType.UserStory),
            project = projectId,
            ref = newUserStoryRef
        )

        return workItemMapper.toDomain(dto = userStory, taskType = CommonTaskType.UserStory)
    }
}
