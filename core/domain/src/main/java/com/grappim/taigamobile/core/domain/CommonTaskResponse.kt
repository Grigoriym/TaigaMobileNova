package com.grappim.taigamobile.core.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class CommonTaskResponse(
    val id: Long,
    val subject: String,
    @Json(name = "created_date")
    val createdDate: LocalDateTime,
    val status: Long,
    val ref: Int,
    @Json(name = "assigned_to_extra_info")
    val assignedToExtraInfo: User?,
    @Json(name = "status_extra_info")
    val statusExtraInfo: StatusExtra,
    @Json(name = "project_extra_info")
    val projectExtraInfo: Project,
    val milestone: Long?,
    @Json(name = "assigned_users")
    val assignedUsers: List<Long>?,
    @Json(name = "assigned_to")
    val assignedTo: Long?,
    val watchers: List<Long>?,
    val owner: Long?,
    val description: String?,
    val epics: List<EpicShortInfo>?,
    @Json(name = "user_story_extra_info")
    val userStoryExtraInfo: UserStoryShortInfo?,
    val version: Int,
    @Json(name = "is_closed")
    val isClosed: Boolean,
    val tags: List<List<String?>>?,
    val swimlane: Long?,
    @Json(name = "due_date")
    val dueDate: LocalDate?,
    @Json(name = "due_date_status")
    val dueDateStatus: DueDateStatus?,
    @Json(name = "blocked_note")
    val blockedNote: String,
    @Json(name = "is_blocked")
    val isBlocked: Boolean,

    // for epic
    val color: String?,

    // for issue
    val type: Long?,
    val severity: Long?,
    val priority: Long?
) {
    @JsonClass(generateAdapter = true)
    data class StatusExtra(val color: String, val name: String)
}
