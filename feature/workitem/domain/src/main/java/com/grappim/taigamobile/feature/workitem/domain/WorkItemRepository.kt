package com.grappim.taigamobile.feature.workitem.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFields
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

interface WorkItemRepository {

    suspend fun getWorkItems(
        commonTaskType: CommonTaskType,
        projectId: Long,
        assignedId: Long? = null,
        isClosed: Boolean? = null,
        watcherId: Long? = null,
        isDashboard: Boolean? = null,
        assignedIds: String? = null
    ): ImmutableList<WorkItem>

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

    /**
     * The name can be changed, but for now it is what it is
     * Mostly we will use  this function in delegates to retrieve some specific data
     */
    suspend fun getUpdateWorkItem(workItemId: Long, commonTaskType: CommonTaskType): UpdateWorkItem

    suspend fun updateWatchersData(
        version: Long,
        workItemId: Long,
        newWatchers: ImmutableList<Long>,
        commonTaskType: CommonTaskType
    ): WatchersListUpdateData

    suspend fun getCustomFields(workItemId: Long, commonTaskType: CommonTaskType): CustomFields

    suspend fun getWorkItemAttachments(workItemId: Long, commonTaskType: CommonTaskType): ImmutableList<Attachment>

    suspend fun deleteWorkItem(workItemId: Long, commonTaskType: CommonTaskType)

    suspend fun patchWikiPage(pageId: Long, version: Long, payload: ImmutableMap<String, Any?>): PatchedData

    suspend fun createWorkItem(
        commonTaskType: CommonTaskType,
        subject: String,
        description: String,
        status: Long?
    ): WorkItem
}
