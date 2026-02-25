package com.grappim.taigamobile.testing

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.feature.workitem.domain.Attachment
import com.grappim.taigamobile.feature.workitem.domain.PatchedCustomAttributes
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.UpdateWorkItem
import com.grappim.taigamobile.feature.workitem.domain.WatchersListUpdateData
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFields
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf

data class GetWorkItemsCall(
    val commonTaskType: CommonTaskType,
    val projectId: Long,
    val assignedId: Long?,
    val isClosed: Boolean?,
    val watcherId: Long?,
    val modifiedDateGte: String?,
    val finishDateGte: String?,
)

class FakeWorkItemRepository : WorkItemRepository {

    val itemsByType = mutableMapOf<CommonTaskType, ImmutableList<WorkItem>>()
    var error: Exception? = null
    val calls = mutableListOf<GetWorkItemsCall>()

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
        pageSize: Int?,
    ): ImmutableList<WorkItem> {
        error?.let { throw it }
        calls += GetWorkItemsCall(
            commonTaskType = commonTaskType,
            projectId = projectId,
            assignedId = assignedId,
            isClosed = isClosed,
            watcherId = watcherId,
            modifiedDateGte = modifiedDateGte,
            finishDateGte = finishDateGte,
        )
        return itemsByType[commonTaskType] ?: persistentListOf()
    }

    override suspend fun patchData(
        version: Long,
        workItemId: Long,
        payload: ImmutableMap<String, Any?>,
        commonTaskType: CommonTaskType,
    ): PatchedData = error("not used in this test")

    override suspend fun patchCustomAttributes(
        customAttributesVersion: Long,
        workItemId: Long,
        payload: ImmutableMap<String, Any?>,
        commonTaskType: CommonTaskType,
    ): PatchedCustomAttributes = error("not used in this test")

    override suspend fun addAttachment(
        workItemId: Long,
        fileName: String,
        fileByteArray: ByteArray,
        projectId: Long,
        taskIdentifier: TaskIdentifier,
    ): Attachment = error("not used in this test")

    override suspend fun deleteAttachment(
        attachment: Attachment,
        taskIdentifier: TaskIdentifier,
    ): Unit = error("not used in this test")

    override suspend fun getWorkItemAttachments(
        workItemId: Long,
        taskIdentifier: TaskIdentifier,
    ): ImmutableList<Attachment> = error("not used in this test")

    override suspend fun watchWorkItem(
        workItemId: Long,
        commonTaskType: CommonTaskType,
    ): Unit = error("not used in this test")

    override suspend fun unwatchWorkItem(
        workItemId: Long,
        commonTaskType: CommonTaskType,
    ): Unit = error("not used in this test")

    override suspend fun getUpdateWorkItem(
        workItemId: Long,
        commonTaskType: CommonTaskType,
    ): UpdateWorkItem = error("not used in this test")

    override suspend fun updateWatchersData(
        version: Long,
        workItemId: Long,
        newWatchers: ImmutableList<Long>,
        commonTaskType: CommonTaskType,
    ): WatchersListUpdateData = error("not used in this test")

    override suspend fun getCustomFields(
        workItemId: Long,
        commonTaskType: CommonTaskType,
    ): CustomFields = error("not used in this test")

    override suspend fun deleteWorkItem(
        workItemId: Long,
        commonTaskType: CommonTaskType,
    ): Unit = error("not used in this test")

    override suspend fun patchWikiPage(
        pageId: Long,
        version: Long,
        payload: ImmutableMap<String, Any?>,
    ): PatchedData = error("not used in this test")

    override suspend fun createWorkItem(
        commonTaskType: CommonTaskType,
        subject: String,
        description: String,
        status: Long?,
    ): WorkItem = error("not used in this test")

    override suspend fun promoteToUserStory(
        workItemId: Long,
        commonTaskType: CommonTaskType,
    ): WorkItem = error("not used in this test")
}
