package com.grappim.taigamobile.feature.kanban.domain

import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import kotlinx.collections.immutable.ImmutableList

data class KanbanUserStory(val userStory: UserStory, val assignees: ImmutableList<TeamMember>)
