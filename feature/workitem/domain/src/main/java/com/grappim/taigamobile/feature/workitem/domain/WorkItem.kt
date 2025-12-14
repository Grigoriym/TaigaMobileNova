package com.grappim.taigamobile.feature.workitem.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.users.domain.User
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDateTime

data class WorkItem(
    val id: Long,
    val taskType: CommonTaskType,
    val createdDate: LocalDateTime,
    val status: Status,
    val ref: Int,
    val title: String,
    val isBlocked: Boolean,
    val tags: ImmutableList<Tag>,
    val isClosed: Boolean,
    val colors: ImmutableList<String> = persistentListOf(),
    val assignee: User? = null
)
