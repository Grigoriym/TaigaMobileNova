package com.grappim.taigamobile.feature.sprint.domain

import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

data class SprintData(
    val sprint: Sprint,
    val statuses: ImmutableList<Statuses>,
    val storiesWithTasks: ImmutableMap<WorkItem, ImmutableList<WorkItem>>,
    val issues: ImmutableList<WorkItem>,
    val storylessTasks: ImmutableList<WorkItem>
)
