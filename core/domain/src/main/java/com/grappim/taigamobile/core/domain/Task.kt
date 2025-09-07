package com.grappim.taigamobile.core.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Tasks related entities
 */
data class StatusOld(val id: Long, val name: String, val color: String, val type: StatusType)

enum class StatusType {
    Status,
    Type,
    Severity,
    Priority
}

enum class CommonTaskType {
    UserStory,
    Task,
    Epic,
    Issue
}

data class CommonTask(
    val id: Long,
    val createdDate: LocalDateTime,
    val title: String,
    val ref: Int,
    val statusOld: StatusOld,
    val assignee: UserDTO? = null,
    val projectDTOInfo: ProjectDTO,
    val taskType: CommonTaskType,
    val isClosed: Boolean,
    val tags: List<Tag> = emptyList(),
    // colored indicators (for stories and epics)
    val colors: List<String> = emptyList(),
    val blockedNote: String? = null
)

@JsonClass(generateAdapter = false)
enum class DueDateStatusDTO {
    @Json(name = "not_set")
    NotSet,

    @Json(name = "set")
    Set,

    @Json(name = "due_soon")
    DueSoon,

    @Json(name = "past_due")
    PastDue,

    @Json(name = "no_longer_applicable")
    NoLongerApplicable
}

data class CommonTaskExtended(
    val id: Long,
    val statusOld: StatusOld,
    val taskType: CommonTaskType,
    val createdDateTime: LocalDateTime,
    val sprint: Sprint?,
    val assignedIds: List<Long>,
    val watcherIds: List<Long>,
    val creatorId: Long,
    val ref: Int,
    val title: String,
    val isClosed: Boolean,
    val description: String,
    val projectSlug: String,
    val version: Long,
    val epicsShortInfo: List<EpicShortInfo> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val swimlaneDTO: SwimlaneDTO?,
    val dueDate: LocalDate?,
    val dueDateStatusDTO: DueDateStatusDTO?,
    val userStoryShortInfo: UserStoryShortInfo? = null,
    val url: String,
    val blockedNote: String? = null,

    // for epic
    val color: String? = null,

    // for issue
    val type: StatusOld? = null,
    val priority: StatusOld? = null,
    val severity: StatusOld? = null
)

@JsonClass(generateAdapter = true)
data class EpicShortInfo(
    val id: Long,
    @Json(name = "subject") val title: String,
    val ref: Int,
    val color: String
)

@JsonClass(generateAdapter = true)
data class UserStoryShortInfo(
    val id: Long,
    val ref: Int,
    @Json(name = "subject") val title: String,
    val epics: List<EpicShortInfo>?
) {
    val epicColors get() = epics?.map { it.color }.orEmpty()
}
