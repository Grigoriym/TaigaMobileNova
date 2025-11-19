package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.core.api.AttachmentMapper
import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toPersistentMap
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class WorkItemRepositoryImpl @Inject constructor(
    private val workItemApi: WorkItemApi,
    private val patchedDataMapper: PatchedDataMapper,
    private val attachmentMapper: AttachmentMapper
) : WorkItemRepository {

    override suspend fun patchData(
        version: Long,
        id: Long,
        payload: ImmutableMap<String, Any?>,
        taskPath: WorkItemPathPlural
    ): PatchedData {
        val editedMap = payload.toPersistentMap().put("version", version)
        val result = workItemApi.patchWorkItem(
            taskPath = taskPath,
            id = id,
            payload = editedMap
        )
        return patchedDataMapper.toDomain(result)
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
}
