package com.grappim.taigamobile.feature.kanban.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.kanban.domain.GetKanbanDataUseCase
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.filters.UsersFilters
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class KanbanViewModel @Inject constructor(
    private val getKanbanDataUseCase: GetKanbanDataUseCase,
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
                _state.update { it.copy(activeFilters = filters) }
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
            getKanbanDataUseCase.getData()
                .onSuccess { result ->
                    val filters = buildAssigneeFilters(
                        teamMembers = result.teamMembers,
                        stories = result.stories
                    )
                    val updatedActiveFilters = _state.value.activeFilters.updateData(filters)

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
                            storiesByStatus = result.storiesByStatus,
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
            val newStoriesByStatus = getKanbanDataUseCase.computeStoriesByStatus(
                stories = currentState.stories,
                statuses = currentState.statuses,
                teamMembers = currentState.teamMembers,
                swimlane = swimlane
            )
            _state.update {
                it.copy(
                    selectedSwimlane = swimlane,
                    storiesByStatus = newStoriesByStatus
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
}
