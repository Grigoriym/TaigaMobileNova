package com.grappim.taigamobile.feature.tasks.domain

import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.feature.workitem.domain.Attachment
import com.grappim.taigamobile.feature.workitem.domain.Comment
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFields
import kotlinx.collections.immutable.ImmutableList

data class TaskDetailsData(
    val task: Task,
    val attachments: ImmutableList<Attachment>,
    val sprint: Sprint?,
    val customFields: CustomFields,
    val comments: ImmutableList<Comment>,
    val creator: User,
    val assignees: ImmutableList<User>,
    val watchers: ImmutableList<User>,
    val isAssignedToMe: Boolean,
    val isWatchedByMe: Boolean,
    val filtersData: FiltersData
)
