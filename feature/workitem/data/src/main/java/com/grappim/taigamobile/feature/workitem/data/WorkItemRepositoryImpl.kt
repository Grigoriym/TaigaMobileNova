package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.core.api.AttachmentMapper
import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.domain.patch.PatchedCustomAttributes
import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.WatchersListUpdateData
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class WorkItemRepositoryImpl @Inject constructor(
    private val workItemApi: WorkItemApi,
    private val patchedDataMapper: PatchedDataMapper,
    private val attachmentMapper: AttachmentMapper,
    private val workItemMapper: WorkItemMapper,
    private val usersRepository: UsersRepository
) : WorkItemRepository {

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
        version: Long,
        workItemId: Long,
        payload: ImmutableMap<String, Any?>,
        commonTaskType: CommonTaskType
    ): PatchedCustomAttributes {
        val editedMap = payload.toPersistentMap().put("version", version)
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
        commonTaskType: CommonTaskType
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
            taskPath = WorkItemPathPlural(commonTaskType),
            file = file,
            project = project,
            objectId = objectId
        )
        return attachmentMapper.toDomain(dto)
    }

    override suspend fun deleteAttachment(
        attachment: Attachment,
        commonTaskType: CommonTaskType
    ) {
        workItemApi.deleteAttachment(
            taskPath = WorkItemPathPlural(commonTaskType),
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

    override suspend fun getWorkItem(workItemId: Long, commonTaskType: CommonTaskType): WorkItem {
        val response = workItemApi.getWorkItemById(
            taskPath = WorkItemPathPlural(commonTaskType),
            id = workItemId
        )
        return workItemMapper.toDomain(response)
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
}
