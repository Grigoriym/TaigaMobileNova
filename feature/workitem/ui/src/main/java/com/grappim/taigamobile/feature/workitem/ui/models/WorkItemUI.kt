package com.grappim.taigamobile.feature.workitem.ui.models

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.User
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * This one should be used on UI when we have a list of work items, e.g.
 * Epic can show user stories in the details screen
 */
data class WorkItemUI(
    val id: Long,
    val taskType: CommonTaskType,
    val createdDate: String,
    val status: StatusUI,
    val ref: Int,
    val title: String,
    val isBlocked: Boolean,
    val tags: ImmutableList<TagUI>,
    val isClosed: Boolean,
    val colors: ImmutableList<String> = persistentListOf(),
    val assignee: User? = null
)
