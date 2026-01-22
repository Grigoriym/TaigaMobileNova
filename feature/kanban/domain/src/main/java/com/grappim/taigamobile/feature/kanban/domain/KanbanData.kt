package com.grappim.taigamobile.feature.kanban.domain

import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

data class KanbanData(
    val statuses: ImmutableList<Statuses>,
    val stories: ImmutableList<UserStory>,
    val swimlanes: ImmutableList<Swimlane>,
    val teamMembers: ImmutableList<TeamMember>,
    val canAddUserStory: Boolean,
    val defaultSwimlane: Swimlane?,
    val storiesByStatus: ImmutableMap<Statuses, ImmutableList<KanbanUserStory>>
)
