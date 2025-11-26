package com.grappim.taigamobile.feature.workitem.domain

import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.CustomFields
import com.grappim.taigamobile.core.domain.patch.PatchedCustomAttributes
import com.grappim.taigamobile.core.domain.patch.PatchedData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

interface WorkItemRepository {
    suspend fun patchData(
        version: Long,
        workItemId: Long,
        payload: ImmutableMap<String, Any?>,
        commonTaskType: CommonTaskType
    ): PatchedData

    suspend fun patchCustomAttributes(
        customAttributesVersion: Long,
        workItemId: Long,
        payload: ImmutableMap<String, Any?>,
        commonTaskType: CommonTaskType
    ): PatchedCustomAttributes

    suspend fun addAttachment(
        workItemId: Long,
        fileName: String,
        fileByteArray: ByteArray,
        projectId: Long,
        commonTaskType: CommonTaskType
    ): Attachment

    suspend fun deleteAttachment(attachment: Attachment, commonTaskType: CommonTaskType)

    suspend fun watchWorkItem(workItemId: Long, commonTaskType: CommonTaskType)

    suspend fun unwatchWorkItem(workItemId: Long, commonTaskType: CommonTaskType)

    suspend fun getWorkItem(workItemId: Long, commonTaskType: CommonTaskType): WorkItem

    suspend fun updateWatchersData(
        version: Long,
        workItemId: Long,
        newWatchers: ImmutableList<Long>,
        commonTaskType: CommonTaskType
    ): WatchersListUpdateData

    suspend fun getCustomFields(workItemId: Long, commonTaskType: CommonTaskType): CustomFields

    suspend fun getWorkItemAttachments(workItemId: Long, commonTaskType: CommonTaskType): ImmutableList<Attachment>

    suspend fun deleteWorkItem(workItemId: Long, commonTaskType: CommonTaskType)
}
