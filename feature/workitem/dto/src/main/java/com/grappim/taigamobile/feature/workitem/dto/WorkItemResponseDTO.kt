package com.grappim.taigamobile.feature.workitem.dto

import com.grappim.taigamobile.core.serialization.LocalDateSerializer
import com.grappim.taigamobile.core.serialization.LocalDateTimeSerializer
import com.grappim.taigamobile.feature.epics.dto.EpicShortInfoDTO
import com.grappim.taigamobile.feature.projects.dto.ProjectDTO
import com.grappim.taigamobile.feature.projects.dto.ProjectExtraInfoDTO
import com.grappim.taigamobile.feature.users.dto.UserDTO
import com.grappim.taigamobile.feature.userstories.dto.UserStoryShortInfoDTO
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class WorkItemResponseDTO(
    val id: Long,
    val subject: String,
    @SerialName(value = "created_date")
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdDate: LocalDateTime,
    val status: Long,
    val ref: Long,
    @SerialName(value = "assigned_to_extra_info")
    val assignedToExtraInfo: UserDTO?,
    @SerialName(value = "status_extra_info")
    val statusExtraInfo: StatusExtraInfoDTO,
    @SerialName(value = "project_extra_info")
    val projectDTOExtraInfo: ProjectExtraInfoDTO,
    val milestone: Long?,
    @SerialName(value = "assigned_users")
    val assignedUsers: List<Long>?,
    @SerialName(value = "assigned_to")
    val assignedTo: Long?,
    val watchers: List<Long>?,
    val owner: Long?,
    val description: String?,
    val epics: List<EpicShortInfoDTO>?,
    @SerialName(value = "user_story_extra_info")
    val userStoryExtraInfo: UserStoryShortInfoDTO?,
    val version: Long,
    @SerialName(value = "is_closed")
    val isClosed: Boolean,
    val tags: List<List<String?>>?,
    val swimlane: Long?,
    @Serializable(with = LocalDateSerializer::class)
    @SerialName(value = "due_date")
    val dueDate: LocalDate?,
    @SerialName(value = "due_date_status")
    val dueDateStatusDTO: DueDateStatusDTO?,
    @SerialName(value = "blocked_note")
    val blockedNote: String,
    @SerialName(value = "is_blocked")
    val isBlocked: Boolean,

    // for epic
    val color: String?,

    // for issue
    val type: Long?,
    val severity: Long?,
    val priority: Long?,

    @SerialName(value = "generated_user_stories")
    val generatedUserStories: List<GeneratedUserStoryDTO>?,
    @SerialName(value = "from_task_ref")
    val fromTaskRef: String?,
    @SerialName("kanban_order")
    val kanbanOrder: Long
)
