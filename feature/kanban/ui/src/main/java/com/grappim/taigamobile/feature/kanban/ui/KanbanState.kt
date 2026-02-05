package com.grappim.taigamobile.feature.kanban.ui

import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.kanban.domain.KanbanUserStory
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

data class KanbanState(
    val statuses: ImmutableList<Statuses> = persistentListOf(),
    val swimlanes: ImmutableList<Swimlane> = persistentListOf(),
    val storiesByStatus: ImmutableMap<Statuses, ImmutableList<KanbanUserStory>> = persistentMapOf(),

    val stories: ImmutableList<UserStory> = persistentListOf(),
    val teamMembers: ImmutableList<TeamMember> = persistentListOf(),

    val onRefresh: () -> Unit,
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty,

    val selectedSwimlane: Swimlane? = null,
    val onSelectSwimlane: (Swimlane?) -> Unit,

    val filters: FiltersData = FiltersData(),
    val activeFilters: FiltersData = FiltersData(),
    val filtersBySwimlane: ImmutableMap<Long?, FiltersData> = persistentMapOf(),
    val filtersError: NativeText = NativeText.Empty,
    val isFiltersLoading: Boolean = false,
    val onSelectFilters: (FiltersData) -> Unit = {},
    val onRetryFilters: () -> Unit = {},

    val canAddUserStory: Boolean = false,

    val onMoveStory:
    (storyId: Long, newStatusId: Long, swimlaneId: Long?, beforeStoryId: Long?, afterStoryId: Long?) -> Unit = {
            _,
            _,
            _,
            _,
            _
        ->
    }
)
