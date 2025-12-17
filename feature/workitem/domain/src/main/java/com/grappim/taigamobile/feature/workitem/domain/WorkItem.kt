package com.grappim.taigamobile.feature.workitem.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.projects.domain.Project
import com.grappim.taigamobile.feature.users.domain.User
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDateTime

/**
 * It is used as a simplified version of a specific WorkItem, for example in lists
 */
data class WorkItem(
    val id: Long,
    val taskType: CommonTaskType,
    val createdDate: LocalDateTime,
    val status: Status,
    val ref: Long,
    val title: String,
    val isBlocked: Boolean,
    val tags: ImmutableList<Tag>,
    val isClosed: Boolean,
    val colors: ImmutableList<String> = persistentListOf(),
    val assignee: User? = null,
    val blockedNote: String? = null,
    val project: Project
)
