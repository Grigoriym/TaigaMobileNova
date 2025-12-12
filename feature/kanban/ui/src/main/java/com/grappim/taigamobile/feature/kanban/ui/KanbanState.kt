package com.grappim.taigamobile.feature.kanban.ui

import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.lang.annotation.Native

data class KanbanState(
    val teamMembers: ImmutableList<TeamMember> = persistentListOf(),
    val statuses: ImmutableList<Statuses> = persistentListOf(),
    val stories: ImmutableList<UserStory> = persistentListOf(),
    val swimlanes: ImmutableList<Swimlane> = persistentListOf(),

    val onRefresh: () -> Unit,
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty,

    val selectedSwimlane: Swimlane? = null,
    val onSelectSwimlane: (Swimlane?) -> Unit
)
