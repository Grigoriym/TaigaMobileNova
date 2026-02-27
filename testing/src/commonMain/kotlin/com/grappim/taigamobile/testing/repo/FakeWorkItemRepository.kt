package com.grappim.taigamobile.testing.repo

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

class AddAttachmentCall(
    val workItemId: Long,
    val fileName: String,
    val fileByteArray: ByteArray,
    val projectId: Long,
    val taskIdentifier: TaskIdentifier,
)

data class DeleteAttachmentCall(
    val attachment: Attachment,
    val taskIdentifier: TaskIdentifier,
)

data class GetWorkItemsCall(
    val commonTaskType: CommonTaskType,
    val projectId: Long,
    val assignedId: Long?,
    val isClosed: Boolean?,
    val watcherId: Long?,
    val modifiedDateGte: String?,
    val finishDateGte: String?,
)

data class PatchDataCall(
    val version: Long,
    val workItemId: Long,
    val payload: ImmutableMap<String, Any?>,
    val commonTaskType: CommonTaskType,
)

data class PatchWikiPageCall(
    val pageId: Long,
    val version: Long,
    val payload: ImmutableMap<String, Any?>,
)

class FakeWorkItemRepository : WorkItemRepository {

    val itemsByType = mutableMapOf<CommonTaskType, ImmutableList<WorkItem>>()
    var error: Exception? = null
    val calls = mutableListOf<GetWorkItemsCall>()

    var patchDataResult: PatchedData? = null
    var patchDataThrows: Throwable? = null
    val patchDataCalls: MutableList<PatchDataCall> = mutableListOf()

    var promoteToUserStoryThrows: Throwable? = null
    var promoteToUserStoryResult: WorkItem? = null
    var promoteToUserStoryCalled = false

    var deleteWorkItemThrows: Throwable? = null
    var deleteWorkItemCalled = false

    var addAttachmentResult: Attachment? = null
    var addAttachmentThrows: Throwable? = null
    val addAttachmentCalls: MutableList<AddAttachmentCall> = mutableListOf()

    var deleteAttachmentThrows: Throwable? = null
    val deleteAttachmentCalls: MutableList<DeleteAttachmentCall> = mutableListOf()

    var patchWikiPageResult: PatchedData? = null
    var patchWikiPageThrows: Throwable? = null
    val patchWikiPageCalls: MutableList<PatchWikiPageCall> = mutableListOf()

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
    ): PatchedData {
        patchDataCalls += PatchDataCall(version, workItemId, payload, commonTaskType)
        patchDataThrows?.let { throw it }
        return patchDataResult ?: error("patchDataResult not configured")
    }

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
    ): Attachment {
        addAttachmentCalls += AddAttachmentCall(workItemId, fileName, fileByteArray, projectId, taskIdentifier)
        addAttachmentThrows?.let { throw it }
        return addAttachmentResult ?: error("addAttachmentResult not configured")
    }

    override suspend fun deleteAttachment(
        attachment: Attachment,
        taskIdentifier: TaskIdentifier,
    ) {
        deleteAttachmentCalls += DeleteAttachmentCall(attachment, taskIdentifier)
        deleteAttachmentThrows?.let { throw it }
    }

    override suspend fun getWorkItemAttachments(
        workItemId: Long,
        taskIdentifier: TaskIdentifier,
    ): ImmutableList<Attachment> = error("not used in this test")

    var watchWorkItemThrows: Throwable? = null
    var watchWorkItemCalled = false

    var unwatchWorkItemThrows: Throwable? = null
    var unwatchWorkItemCalled = false

    var getUpdateWorkItemResult: UpdateWorkItem? = null
    var getUpdateWorkItemThrows: Throwable? = null

    var updateWatchersDataResult: WatchersListUpdateData? = null
    var updateWatchersDataThrows: Throwable? = null

    override suspend fun watchWorkItem(
        workItemId: Long,
        commonTaskType: CommonTaskType,
    ) {
        watchWorkItemCalled = true
        watchWorkItemThrows?.let { throw it }
    }

    override suspend fun unwatchWorkItem(
        workItemId: Long,
        commonTaskType: CommonTaskType,
    ) {
        unwatchWorkItemCalled = true
        unwatchWorkItemThrows?.let { throw it }
    }

    override suspend fun getUpdateWorkItem(
        workItemId: Long,
        commonTaskType: CommonTaskType,
    ): UpdateWorkItem {
        getUpdateWorkItemThrows?.let { throw it }
        return getUpdateWorkItemResult ?: error("getUpdateWorkItemResult not configured")
    }

    override suspend fun updateWatchersData(
        version: Long,
        workItemId: Long,
        newWatchers: ImmutableList<Long>,
        commonTaskType: CommonTaskType,
    ): WatchersListUpdateData {
        updateWatchersDataThrows?.let { throw it }
        return updateWatchersDataResult ?: error("updateWatchersDataResult not configured")
    }

    override suspend fun getCustomFields(
        workItemId: Long,
        commonTaskType: CommonTaskType,
    ): CustomFields = error("not used in this test")

    override suspend fun deleteWorkItem(
        workItemId: Long,
        commonTaskType: CommonTaskType,
    ) {
        deleteWorkItemCalled = true
        deleteWorkItemThrows?.let { throw it }
    }

    override suspend fun patchWikiPage(
        pageId: Long,
        version: Long,
        payload: ImmutableMap<String, Any?>,
    ): PatchedData {
        patchWikiPageCalls += PatchWikiPageCall(pageId, version, payload)
        patchWikiPageThrows?.let { throw it }
        return patchWikiPageResult ?: error("patchWikiPageResult not configured")
    }

    override suspend fun createWorkItem(
        commonTaskType: CommonTaskType,
        subject: String,
        description: String,
        status: Long?,
    ): WorkItem = error("not used in this test")

    override suspend fun promoteToUserStory(
        workItemId: Long,
        commonTaskType: CommonTaskType,
    ): WorkItem {
        promoteToUserStoryCalled = true
        promoteToUserStoryThrows?.let { throw it }
        return promoteToUserStoryResult ?: error("promoteToUserStoryResult not configured")
    }
}
