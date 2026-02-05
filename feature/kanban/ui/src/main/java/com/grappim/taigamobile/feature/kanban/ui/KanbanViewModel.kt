package com.grappim.taigamobile.feature.kanban.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.filters.UsersFilters
import com.grappim.taigamobile.feature.kanban.domain.GetKanbanDataUseCase
import com.grappim.taigamobile.feature.kanban.domain.KanbanUserStory
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class KanbanViewModel @Inject constructor(
    private val getKanbanDataUseCase: GetKanbanDataUseCase,
    private val taigaSessionStorage: TaigaSessionStorage,
    private val session: Session
) : ViewModel() {

    private val _state = MutableStateFlow(
        KanbanState(
            onRefresh = ::refresh,
            onSelectSwimlane = ::selectSwimlane,
            onSelectFilters = ::selectFilters,
            activeFilters = session.kanbanFilters.value
        )
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            session.kanbanFilters.collect { filters ->
                val currentState = _state.value
                val filteredStoriesByStatus = computeStoriesByStatusWithFilters(
                    stories = currentState.stories,
                    statuses = currentState.statuses,
                    teamMembers = currentState.teamMembers,
                    swimlane = currentState.selectedSwimlane,
                    activeFilters = filters
                )
                _state.update {
                    it.copy(
                        activeFilters = filters,
                        storiesByStatus = filteredStoriesByStatus
                    )
                }
            }
        }

        getKanbanData()
    }

    private fun getKanbanData() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = NativeText.Empty
                )
            }
            getKanbanDataUseCase.getData(
                storageSwimlane = taigaSessionStorage.kanbanDefaultSwimline.first()
            )
                .onSuccess { result ->
                    val filters = buildAssigneeFilters(
                        teamMembers = result.teamMembers,
                        stories = result.stories
                    )
                    val updatedActiveFilters = _state.value.activeFilters.updateData(filters)
                    val storiesByStatus = computeStoriesByStatusWithFilters(
                        stories = result.stories,
                        statuses = result.statuses,
                        teamMembers = result.teamMembers,
                        swimlane = result.defaultSwimlane,
                        activeFilters = updatedActiveFilters
                    )

                    session.changeKanbanFilters(updatedActiveFilters)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            statuses = result.statuses,
                            swimlanes = result.swimlanes,
                            stories = result.stories,
                            teamMembers = result.teamMembers,
                            canAddUserStory = result.canAddUserStory,
                            selectedSwimlane = result.defaultSwimlane,
                            storiesByStatus = storiesByStatus,
                            filters = filters,
                            activeFilters = updatedActiveFilters
                        )
                    }
                }
                .onFailure { error ->
                    Timber.e(error)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = getErrorMessage(error)
                        )
                    }
                }
        }
    }

    private fun refresh() {
        getKanbanData()
    }

    private fun selectSwimlane(swimlane: Swimlane?) {
        val currentState = _state.value
        viewModelScope.launch {
            swimlane?.id?.let { swimlaneId ->
                taigaSessionStorage.setKanbanDefaultSwimline(swimlaneId)
            }

            val newStoriesByStatus = getKanbanDataUseCase.computeStoriesByStatus(
                stories = currentState.stories,
                statuses = currentState.statuses,
                teamMembers = currentState.teamMembers,
                swimlane = swimlane
            )
            val filteredStories = filterStoriesByAssignees(
                storiesByStatus = newStoriesByStatus,
                activeFilters = _state.value.activeFilters
            )
            _state.update {
                it.copy(
                    selectedSwimlane = swimlane,
                    storiesByStatus = filteredStories
                )
            }
        }
    }

    private fun selectFilters(filters: FiltersData) {
        session.changeKanbanFilters(filters)
    }

    private fun buildAssigneeFilters(
        teamMembers: ImmutableList<TeamMember>,
        stories: ImmutableList<UserStory>
    ): FiltersData {
        val counts = mutableMapOf<Long?, Long>()
        teamMembers.forEach { counts[it.id] = 0L }
        counts[null] = 0L

        stories.forEach { story ->
            if (story.assignedUserIds.isEmpty()) {
                counts[null] = counts.getOrDefault(null, 0L) + 1
            } else {
                story.assignedUserIds.forEach { id ->
                    counts[id] = counts.getOrDefault(id, 0L) + 1
                }
            }
        }

        val assigneeFilters = counts.mapNotNull { (id, count) ->
            if (id == null) {
                UsersFilters(
                    id = null,
                    name = "",
                    count = count
                )
            } else {
                teamMembers.find { it.id == id }?.let { member ->
                    UsersFilters(
                        id = member.id,
                        name = member.name,
                        count = count
                    )
                }
            }
        }.sortedBy { it.name }.toImmutableList()

        return FiltersData(
            assignees = assigneeFilters
        )
    }

    private suspend fun computeStoriesByStatusWithFilters(
        stories: ImmutableList<UserStory>,
        statuses: ImmutableList<Statuses>,
        teamMembers: ImmutableList<TeamMember>,
        swimlane: Swimlane?,
        activeFilters: FiltersData
    ): ImmutableMap<Statuses, ImmutableList<KanbanUserStory>> {
        val storiesByStatus = getKanbanDataUseCase.computeStoriesByStatus(
            stories = stories,
            statuses = statuses,
            teamMembers = teamMembers,
            swimlane = swimlane
        )
        return filterStoriesByAssignees(
            storiesByStatus = storiesByStatus,
            activeFilters = activeFilters
        )
    }

    private fun filterStoriesByAssignees(
        storiesByStatus: ImmutableMap<Statuses, ImmutableList<KanbanUserStory>>,
        activeFilters: FiltersData
    ): ImmutableMap<Statuses, ImmutableList<KanbanUserStory>> {
        val selectedAssignees = activeFilters.assignees
        if (selectedAssignees.isEmpty()) {
            return storiesByStatus
        }

        val selectedAssigneeIds = selectedAssignees.mapNotNull { it.id }.toSet()
        val includeUnassigned = selectedAssignees.any { it.id == null }

        return storiesByStatus
            .mapValues { (_, stories) ->
                stories.filter { story ->
                    val assignedIds = story.userStory.assignedUserIds
                    val hasAssignee = assignedIds.any { selectedAssigneeIds.contains(it) }
                    val isUnassigned = assignedIds.isEmpty()
                    hasAssignee || (includeUnassigned && isUnassigned)
                }.toImmutableList()
            }
            .toImmutableMap()
    }
}
