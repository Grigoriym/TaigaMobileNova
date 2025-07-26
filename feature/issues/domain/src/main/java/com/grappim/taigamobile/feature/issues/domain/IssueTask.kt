package com.grappim.taigamobile.feature.issues.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.DueDateStatus
import com.grappim.taigamobile.core.domain.Project
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate
import java.time.LocalDateTime

data class IssueTask(
    val id: Long,
    val version: Long,
    val ref: Int,

    val creatorId: Long,

    val title: String,
    val description: String,

    val createdDateTime: LocalDateTime,

    val dueDate: LocalDate?,
    val dueDateStatus: DueDateStatus?,

    val project: Project,
    val taskType: CommonTaskType = CommonTaskType.Issue,
    val isClosed: Boolean,
    @Deprecated("should be moved out of here and be a separate field")
    val tags: ImmutableList<Tag> = persistentListOf(),
    /**
     * colored indicators (for stories and epics)
     */
    val colors: List<String> = emptyList(),
    val blockedNote: String? = null,

    val assignee: User? = null,
    val assignedUserIds: List<Long>,
    val watcherUserIds: List<Long>,
    val milestone: Long?,
    val copyLinkUrl: String,

    val status: Statuses?,
    val type: Statuses? = null,
    val priority: Statuses? = null,
    val severity: Statuses? = null
)
