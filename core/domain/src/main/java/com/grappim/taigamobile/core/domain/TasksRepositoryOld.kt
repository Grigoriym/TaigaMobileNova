package com.grappim.taigamobile.core.domain

import java.io.InputStream
import java.time.LocalDate

@Deprecated("Needs to be separated into smaller repos")
interface TasksRepositoryOld {
    suspend fun getCommonTask(commonTaskId: Long, type: CommonTaskType): CommonTaskExtended

    suspend fun getAttachments(commonTaskId: Long, type: CommonTaskType): List<AttachmentDTO>

    suspend fun getCustomFields(commonTaskId: Long, type: CommonTaskType): CustomFields

    // ============
    // Edit methods
    // ============

    // edit task
    suspend fun editStatus(commonTask: CommonTaskExtended, statusId: Long, statusType: StatusType)
    suspend fun editSprint(commonTask: CommonTaskExtended, sprintId: Long?)
    suspend fun editAssignees(commonTask: CommonTaskExtended, assignees: List<Long>)
    suspend fun editWatchers(commonTask: CommonTaskExtended, watchers: List<Long>)
    suspend fun editDueDate(commonTask: CommonTaskExtended, date: LocalDate?)
    suspend fun editCommonTaskBasicInfo(commonTask: CommonTaskExtended, title: String, description: String)

    suspend fun editTags(commonTask: CommonTaskExtended, tagOlds: List<TagOld>)
    suspend fun editUserStorySwimlane(commonTask: CommonTaskExtended, swimlaneId: Long?)
    suspend fun editEpicColor(commonTask: CommonTaskExtended, color: String)
    suspend fun editBlocked(commonTask: CommonTaskExtended, blockedNote: String?)

    // related edits
    suspend fun createComment(commonTaskId: Long, commonTaskType: CommonTaskType, comment: String, version: Long)

    suspend fun createCommonTask(
        commonTaskType: CommonTaskType,
        title: String,
        description: String,
        parentId: Long?,
        sprintId: Long?,
        statusId: Long?,
        swimlaneId: Long?
    ): CommonTask

    suspend fun deleteCommonTask(commonTaskType: CommonTaskType, commonTaskId: Long)

    suspend fun promoteCommonTaskToUserStory(commonTaskId: Long, commonTaskType: CommonTaskType): CommonTask

    suspend fun addAttachment(
        commonTaskId: Long,
        commonTaskType: CommonTaskType,
        fileName: String,
        inputStream: InputStream
    )

    suspend fun deleteAttachment(commonTaskType: CommonTaskType, attachmentId: Long)

    suspend fun editCustomFields(
        commonTaskType: CommonTaskType,
        commonTaskId: Long,
        fields: Map<Long, CustomFieldValue?>,
        version: Long
    )
}
