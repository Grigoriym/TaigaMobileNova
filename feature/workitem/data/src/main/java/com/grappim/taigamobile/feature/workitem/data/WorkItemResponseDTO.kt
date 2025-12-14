package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.core.domain.DueDateStatusDTO
import com.grappim.taigamobile.core.domain.EpicShortInfoDTO
import com.grappim.taigamobile.core.domain.ProjectDTO
import com.grappim.taigamobile.core.domain.UserDTO
import com.grappim.taigamobile.core.domain.UserStoryShortInfoDTO
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class WorkItemResponseDTO(
    val id: Long,
    val subject: String,
    @Json(name = "created_date")
    val createdDate: LocalDateTime,
    val status: Long,
    val ref: Int,
    @Json(name = "assigned_to_extra_info")
    val assignedToExtraInfo: UserDTO?,
    @Json(name = "status_extra_info")
    val statusExtraInfo: StatusExtraInfoDTO,
    @Json(name = "project_extra_info")
    val projectDTOExtraInfo: ProjectDTO,
    val milestone: Long?,
    @Json(name = "assigned_users")
    val assignedUsers: List<Long>?,
    @Json(name = "assigned_to")
    val assignedTo: Long?,
    val watchers: List<Long>?,
    val owner: Long?,
    val description: String?,
    val epics: List<EpicShortInfoDTO>?,
    @Json(name = "user_story_extra_info")
    val userStoryExtraInfo: UserStoryShortInfoDTO?,
    val version: Long,
    @Json(name = "is_closed")
    val isClosed: Boolean,
    val tags: List<List<String?>>?,
    val swimlane: Long?,
    @Json(name = "due_date")
    val dueDate: LocalDate?,
    @Json(name = "due_date_status")
    val dueDateStatusDTO: DueDateStatusDTO?,
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
)

@JsonClass(generateAdapter = true)
data class StatusExtraInfoDTO(val color: String, val name: String)
