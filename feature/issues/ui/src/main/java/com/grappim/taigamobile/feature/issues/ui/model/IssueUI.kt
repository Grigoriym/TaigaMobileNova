package com.grappim.taigamobile.feature.issues.ui.model

import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate
import java.time.LocalDateTime

data class IssueUI(
    val id: Long,
    val version: Long,
    val createdDateTime: LocalDateTime,
    val title: String,
    val ref: Int,
    val isClosed: Boolean,
    val blockedNote: String? = null,
    val description: String,
    val copyLinkUrl: String,
    val creatorId: Long,

    val status: StatusUI?,
    val type: StatusUI? = null,
    val priority: StatusUI? = null,
    val severity: StatusUI? = null,

    val assignedUserIds: List<Long>,
    val watcherUserIds: List<Long>,

    val tags: ImmutableList<TagUI> = persistentListOf(),

    val dueDate: LocalDate?,
    val dueDateStatus: DueDateStatus?
)
