package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.logger.logcat
import com.grappim.taigamobile.core.storage.KmpTaigaSessionStorage
import com.grappim.taigamobile.core.storage.db.dao.WorkItemDao
import com.grappim.taigamobile.core.storage.network.NetworkMonitor
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.Attachment
import com.grappim.taigamobile.feature.workitem.domain.PatchedCustomAttributes
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.UpdateWorkItem
import com.grappim.taigamobile.feature.workitem.domain.WatchersListUpdateData
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFields
import com.grappim.taigamobile.feature.workitem.domain.getPluralPath
import com.grappim.taigamobile.feature.workitem.domain.getSingularPath
import com.grappim.taigamobile.feature.workitem.dto.CreateWorkItemRequestDTO
import com.grappim.taigamobile.feature.workitem.dto.PromoteToUserStoryRequestDTO
import com.grappim.taigamobile.feature.workitem.mapper.AttachmentMapper
import com.grappim.taigamobile.feature.workitem.mapper.CustomFieldsMapper
import com.grappim.taigamobile.feature.workitem.mapper.JsonObjectMapper
import com.grappim.taigamobile.feature.workitem.mapper.PatchedDataMapper
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Single

@Single(binds = [WorkItemRepository::class])
class WorkItemRepositoryImpl(
    private val workItemApi: WorkItemApi,
    private val patchedDataMapper: PatchedDataMapper,
    private val attachmentMapper: AttachmentMapper,
    private val workItemMapper: WorkItemMapper,
    private val workItemEntityMapper: WorkItemEntityMapper,
    private val usersRepository: UsersRepository,
    private val customFieldsMapper: CustomFieldsMapper,
    private val taigaSessionStorage: KmpTaigaSessionStorage,
    private val jsonObjectMapper: JsonObjectMapper,
    private val workItemDao: WorkItemDao,
    private val networkMonitor: NetworkMonitor
) : WorkItemRepository {

    override suspend fun getWorkItems(
        commonTaskType: CommonTaskType,
        projectId: Long,
        assignedId: Long?,
        isClosed: Boolean?,
        watcherId: Long?,
        isDashboard: Boolean?,
        assignedIds: String?,
        isBlocked: Boolean?,
        modifiedDateGte: String?,
        finishDateGte: String?,
        milestoneId: Long?,
        pageSize: Int?
    ): ImmutableList<WorkItem> {
        // Try network first if online
        if (networkMonitor.isOnline.value) {
            try {
                val response = workItemApi.getWorkItems(
                    taskPath = commonTaskType.getPluralPath(),
                    project = projectId,
                    assignedId = assignedId,
                    isClosed = isClosed,
                    watcherId = watcherId,
                    isDashboard = isDashboard,
                    isBlocked = isBlocked,
                    modifiedDateGte = modifiedDateGte,
                    finishDateGte = finishDateGte,
                    sprint = milestoneId,
                    pageSize = pageSize
                )
                val items = workItemMapper.toDomainList(response, commonTaskType)
                // Cache results (only for simple queries without filters that are hard to replicate)
                if (assignedId == null && watcherId == null && isBlocked == null &&
                    modifiedDateGte == null && finishDateGte == null
                ) {
                    workItemDao.insertAll(workItemEntityMapper.toEntityList(items, milestoneId))
                }
                return items
            } catch (e: Exception) {
                logcat(throwable = e) {
                    "Error getting work items"
                }
            }
        }

        // Return cached data
        val cached = if (milestoneId != null) {
            workItemDao.getByProjectIdAndSprint(projectId, milestoneId).first()
        } else {
            workItemDao.getByProjectIdAndType(projectId, commonTaskType).first()
        }

        return cached
            .filter { entity ->
                (isClosed == null || entity.isClosed == isClosed) &&
                    (assignedId == null || entity.assigneeId == assignedId)
            }
            .let { workItemEntityMapper.toDomainList(it) }
            .toImmutableList()
    }

    override suspend fun patchData(
        version: Long,
        workItemId: Long,
        payload: ImmutableMap<String, Any?>,
        commonTaskType: CommonTaskType
    ): PatchedData {
        val editedMap = payload.toPersistentMap().put("version", version)
        val result = workItemApi.patchWorkItem(
            taskPath = commonTaskType.getPluralPath(),
            id = workItemId,
            payload = jsonObjectMapper.fromMapToJsonObject(editedMap)
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
            taskPath = commonTaskType.getPluralPath(),
            taskId = workItemId,
            payload = jsonObjectMapper.fromMapToJsonObject(editedMap)
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
        val dto = workItemApi.uploadCommonTaskAttachment(
            taskPath = taskIdentifier.getPluralPath(),
            fileName = fileName,
            fileBytes = fileByteArray,
            projectId = projectId,
            objectId = workItemId
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
            taskPath = commonTaskType.getPluralPath(),
            workItemId = workItemId
        )
    }

    override suspend fun unwatchWorkItem(workItemId: Long, commonTaskType: CommonTaskType) {
        workItemApi.unwatchWorkItem(
            taskPath = commonTaskType.getPluralPath(),
            workItemId = workItemId
        )
    }

    override suspend fun getUpdateWorkItem(workItemId: Long, commonTaskType: CommonTaskType): UpdateWorkItem {
        val response = workItemApi.getWorkItemById(
            taskPath = commonTaskType.getPluralPath(),
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
                    taskPath = commonTaskType.getSingularPath(),
                    projectId = taigaSessionStorage.getCurrentProjectId()
                )
            }
            val values = async {
                workItemApi.getCustomAttributesValues(
                    id = workItemId,
                    taskPath = commonTaskType.getPluralPath()
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
        val projectId = taigaSessionStorage.getCurrentProjectId()

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
            taskPath = commonTaskType.getPluralPath()
        )
    }

    override suspend fun patchWikiPage(pageId: Long, version: Long, payload: ImmutableMap<String, Any?>): PatchedData {
        val editedMap = payload.toPersistentMap().put("version", version)
        val response = workItemApi.patchWikiPage(
            pageId = pageId,
            payload = jsonObjectMapper.fromMapToJsonObject(editedMap)
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
            taskPath = commonTaskType.getPluralPath(),
            createRequest = CreateWorkItemRequestDTO(
                project = taigaSessionStorage.getCurrentProjectId(),
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
        val projectId = taigaSessionStorage.getCurrentProjectId()

        val response = workItemApi.promoteToUserStory(
            taskPath = commonTaskType.getPluralPath(),
            workItemId = workItemId,
            body = PromoteToUserStoryRequestDTO(
                projectId = projectId
            )
        )
        val newUserStoryRef = response.firstOrNull() ?: error("User story ref not found")

        val userStory = workItemApi.getWorkItemByRef(
            taskPath = CommonTaskType.UserStory.getPluralPath(),
            project = projectId,
            ref = newUserStoryRef
        )

        return workItemMapper.toDomain(dto = userStory, taskType = CommonTaskType.UserStory)
    }
}
