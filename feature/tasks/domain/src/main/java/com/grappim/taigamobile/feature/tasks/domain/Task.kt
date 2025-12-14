package com.grappim.taigamobile.feature.tasks.domain

import com.grappim.taigamobile.core.domain.DueDateStatus
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.projects.domain.Project
import com.grappim.taigamobile.feature.workitem.domain.UserStoryShortInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate
import java.time.LocalDateTime

data class Task(
    val id: Long,
    val version: Long,
    val createdDateTime: LocalDateTime,
    val title: String,
    val ref: Int,
    val status: Statuses?,
    val assignee: User? = null,
    val project: Project,
    val isClosed: Boolean,
    val blockedNote: String? = null,
    val description: String,

    val milestone: Long?,
    val creatorId: Long,

    val assignedUserIds: List<Long>,
    val watcherUserIds: List<Long>,

    val tags: ImmutableList<Tag> = persistentListOf(),

    val dueDate: LocalDate?,
    val dueDateStatus: DueDateStatus?,
    val copyLinkUrl: String,

    val userStory: UserStoryShortInfo?
)
