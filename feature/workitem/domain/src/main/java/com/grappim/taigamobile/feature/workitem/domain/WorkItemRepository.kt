package com.grappim.taigamobile.feature.workitem.domain

import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.patch.PatchedData
import kotlinx.collections.immutable.ImmutableMap

interface WorkItemRepository {
    suspend fun patchData(
        version: Long,
        id: Long,
        payload: ImmutableMap<String, Any?>,
        taskPath: WorkItemPathPlural
    ): PatchedData

    suspend fun addAttachment(
        workItemId: Long,
        fileName: String,
        fileByteArray: ByteArray,
        projectId: Long,
        commonTaskType: CommonTaskType
    ): Attachment

    suspend fun deleteAttachment(
        attachment: Attachment,
        commonTaskType: CommonTaskType
    )
}
