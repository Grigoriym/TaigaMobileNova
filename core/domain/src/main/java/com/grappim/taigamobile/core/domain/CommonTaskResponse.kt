package com.grappim.taigamobile.core.domain

import com.squareup.moshi.JsonClass
import java.time.LocalDate
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class CommonTaskResponse(
    val id: Long,
    val subject: String,
    val created_date: LocalDateTime,
    val status: Long,
    val ref: Int,
    val assigned_to_extra_info: User?,
    val status_extra_info: StatusExtra,
    val project_extra_info: Project,
    val milestone: Long?,
    val assigned_users: List<Long>?,
    val assigned_to: Long?,
    val watchers: List<Long>?,
    val owner: Long?,
    val description: String?,
    val epics: List<EpicShortInfo>?,
    val user_story_extra_info: UserStoryShortInfo?,
    val version: Int,
    val is_closed: Boolean,
    val tags: List<List<String?>>?,
    val swimlane: Long?,
    val due_date: LocalDate?,
    val due_date_status: DueDateStatus?,
    val blocked_note: String,
    val is_blocked: Boolean,

    // for epic
    val color: String?,

    // for issue
    val type: Long?,
    val severity: Long?,
    val priority: Long?
) {
    @JsonClass(generateAdapter = true)
    data class StatusExtra(
        val color: String,
        val name: String
    )
}
