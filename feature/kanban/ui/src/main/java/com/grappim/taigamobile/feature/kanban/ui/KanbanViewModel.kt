package com.grappim.taigamobile.feature.kanban.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.kanban.domain.GetKanbanDataUseCase
import com.grappim.taigamobile.feature.kanban.domain.KanbanUserStory
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentMap
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
    private val userStoriesRepository: UserStoriesRepository,
    private val filtersRepository: FiltersRepository,
    private val session: Session
) : ViewModel() {

    private val initialFilters = session.kanbanFilters.value

    private val _state = MutableStateFlow(
        KanbanState(
            onRefresh = ::refresh,
            onSelectSwimlane = ::selectSwimlane,
            onSelectFilters = ::selectFilters,
            onRetryFilters = ::loadFiltersData,
            onMoveStory = ::moveStory,
            activeFilters = initialFilters,
            filtersBySwimlane = persistentMapOf(null to initialFilters)
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

                val updatedFiltersBySwimlane = currentState.filtersBySwimlane
                    .toMutableMap()
                    .apply { put(currentState.selectedSwimlane?.id, filters) }
                    .toPersistentMap()

                _state.update {
                    it.copy(
                        activeFilters = filters,
                        storiesByStatus = filteredStoriesByStatus,
                        filtersBySwimlane = updatedFiltersBySwimlane
                    )
                }
            }
        }

        getKanbanData()
        loadFiltersData()
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
            ).onSuccess { result ->
                val defaultSwimlaneId = result.defaultSwimlane?.id
                val activeFiltersForSwimlane = _state.value.filtersBySwimlane[defaultSwimlaneId]
                    ?: _state.value.activeFilters
                val filtersBySwimlane = _state.value.filtersBySwimlane
                    .toMutableMap()
                    .apply { put(defaultSwimlaneId, activeFiltersForSwimlane) }
                    .toPersistentMap()
                val storiesByStatus = computeStoriesByStatusWithFilters(
                    stories = result.stories,
                    statuses = result.statuses,
                    teamMembers = result.teamMembers,
                    swimlane = result.defaultSwimlane,
                    activeFilters = activeFiltersForSwimlane
                )

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
                        filters = it.filters,
                        activeFilters = activeFiltersForSwimlane,
                        filtersBySwimlane = filtersBySwimlane
                    )
                }
            }.onFailure { error ->
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

    private fun loadFiltersData() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isFiltersLoading = true,
                    filtersError = NativeText.Empty
                )
            }

            runCatching { filtersRepository.getFiltersData(CommonTaskType.UserStory) }
                .onSuccess { result ->
                    val currentSwimlaneId = _state.value.selectedSwimlane?.id
                    val updatedActiveFilters = _state.value.activeFilters.updateData(result)

                    session.changeKanbanFilters(updatedActiveFilters)

                    val updatedFiltersBySwimlane = _state.value.filtersBySwimlane
                        .toMutableMap()
                        .apply { put(currentSwimlaneId, updatedActiveFilters) }
                        .toPersistentMap()

                    val filteredStories = computeStoriesByStatusWithFilters(
                        stories = _state.value.stories,
                        statuses = _state.value.statuses,
                        teamMembers = _state.value.teamMembers,
                        swimlane = _state.value.selectedSwimlane,
                        activeFilters = updatedActiveFilters
                    )

                    _state.update {
                        it.copy(
                            isFiltersLoading = false,
                            filters = result,
                            activeFilters = updatedActiveFilters,
                            filtersBySwimlane = updatedFiltersBySwimlane,
                            storiesByStatus = filteredStories
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isFiltersLoading = false,
                            filtersError = getErrorMessage(error)
                        )
                    }
                }
        }
    }

    private fun selectSwimlane(swimlane: Swimlane?) {
        val currentState = _state.value
        viewModelScope.launch {
            swimlane?.id?.let { swimlaneId ->
                taigaSessionStorage.setKanbanDefaultSwimline(swimlaneId)
            }

            val swimlaneFilters = currentState.filtersBySwimlane[swimlane?.id]
                ?: FiltersData()

            val filteredStories = computeStoriesByStatusWithFilters(
                stories = currentState.stories,
                statuses = currentState.statuses,
                teamMembers = currentState.teamMembers,
                swimlane = swimlane,
                activeFilters = swimlaneFilters
            )

            val updatedFiltersBySwimlane = currentState.filtersBySwimlane
                .toMutableMap()
                .apply { put(swimlane?.id, swimlaneFilters) }
                .toPersistentMap()

            _state.update {
                it.copy(
                    selectedSwimlane = swimlane,
                    storiesByStatus = filteredStories,
                    activeFilters = swimlaneFilters,
                    filtersBySwimlane = updatedFiltersBySwimlane
                )
            }
        }
    }

    private fun moveStory(
        storyId: Long,
        newStatusId: Long,
        swimlaneId: Long?,
        beforeStoryId: Long?,
        afterStoryId: Long?
    ) {
        val currentState = _state.value
        val previousStoriesByStatus = currentState.storiesByStatus

        val movedStory = previousStoriesByStatus.values
            .flatten()
            .find { it.userStory.id == storyId }

        val movedStorySwimlane = movedStory?.userStory?.swimlane ?: swimlaneId

        val targetStatus = currentState.statuses.find { it.id == newStatusId }
        val targetColumnStories = targetStatus?.let { previousStoriesByStatus[it] }.orEmpty()
        val storiesInSameSwimlane = targetColumnStories.filter {
            it.userStory.swimlane == movedStorySwimlane
        }
        val validStoryIds = storiesInSameSwimlane.map { it.userStory.id }.toSet()

        val validatedBeforeStoryId = beforeStoryId?.takeIf { it in validStoryIds }
        val validatedAfterStoryId = afterStoryId?.takeIf { it in validStoryIds }

        val newStoriesByStatus = computeOptimisticUpdate(
            currentStoriesByStatus = previousStoriesByStatus,
            storyId = storyId,
            newStatusId = newStatusId,
            beforeStoryId = validatedBeforeStoryId
        )

        _state.update { it.copy(storiesByStatus = newStoriesByStatus) }

        viewModelScope.launch {
            runCatching {
                userStoriesRepository.bulkUpdateKanbanOrder(
                    statusId = newStatusId,
                    storyIds = listOf(storyId),
                    swimlaneId = movedStorySwimlane,
                    beforeStoryId = validatedBeforeStoryId,
                    afterStoryId = if (validatedBeforeStoryId == null) validatedAfterStoryId else null
                )
            }.onFailure { error ->
                Timber.e(error, "Failed to move story")
                _state.update {
                    it.copy(
                        storiesByStatus = previousStoriesByStatus,
                        error = getErrorMessage(error)
                    )
                }
            }
        }
    }

    private fun computeOptimisticUpdate(
        currentStoriesByStatus: ImmutableMap<Statuses, ImmutableList<KanbanUserStory>>,
        storyId: Long,
        newStatusId: Long,
        beforeStoryId: Long?
    ): ImmutableMap<Statuses, ImmutableList<KanbanUserStory>> {
        var movedStory: KanbanUserStory? = null

        val withoutStory = currentStoriesByStatus.mapValues { (_, stories) ->
            stories.filter { kanbanStory ->
                if (kanbanStory.userStory.id == storyId) {
                    movedStory = kanbanStory
                    false
                } else {
                    true
                }
            }.toImmutableList()
        }.toImmutableMap()

        if (movedStory == null) return currentStoriesByStatus

        return withoutStory.mapValues { (status, stories) ->
            if (status.id == newStatusId) {
                val mutableStories = stories.toMutableList()
                val insertIndex = if (beforeStoryId != null) {
                    mutableStories.indexOfFirst { it.userStory.id == beforeStoryId }
                        .takeIf { it >= 0 } ?: mutableStories.size
                } else {
                    mutableStories.size
                }
                mutableStories.add(insertIndex, movedStory)
                mutableStories.toImmutableList()
            } else {
                stories
            }
        }.toImmutableMap()
    }

    private fun selectFilters(filters: FiltersData) {
        val currentSwimlaneId = _state.value.selectedSwimlane?.id
        session.changeKanbanFilters(filters)

        val updatedFiltersBySwimlane = _state.value.filtersBySwimlane
            .toMutableMap()
            .apply { put(currentSwimlaneId, filters) }
            .toPersistentMap()

        viewModelScope.launch {
            val filteredStories = computeStoriesByStatusWithFilters(
                stories = _state.value.stories,
                statuses = _state.value.statuses,
                teamMembers = _state.value.teamMembers,
                swimlane = _state.value.selectedSwimlane,
                activeFilters = filters
            )

            _state.update {
                it.copy(
                    activeFilters = filters,
                    storiesByStatus = filteredStories,
                    filtersBySwimlane = updatedFiltersBySwimlane
                )
            }
        }
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
        return filterStories(
            storiesByStatus = storiesByStatus,
            activeFilters = activeFilters
        )
    }

    private fun filterStories(
        storiesByStatus: ImmutableMap<Statuses, ImmutableList<KanbanUserStory>>,
        activeFilters: FiltersData
    ): ImmutableMap<Statuses, ImmutableList<KanbanUserStory>> {
        val selectedAssigneeIds = activeFilters.assignees.mapNotNull { it.id }.toSet()
        val includeUnassigned = activeFilters.assignees.any { it.id == null }

        val selectedCreatorIds = activeFilters.createdBy.mapNotNull { it.id }.toSet()
        val selectedTags = activeFilters.tags.map { it.name }.toSet()
        val selectedEpicIds = activeFilters.epics.map { it.id }.toSet()
        val selectedRoles = activeFilters.roles.map { it.name }.toSet()

        val hasNoFilters = selectedAssigneeIds.isEmpty() && !includeUnassigned &&
            selectedCreatorIds.isEmpty() &&
            selectedTags.isEmpty() &&
            selectedEpicIds.isEmpty() &&
            selectedRoles.isEmpty()

        if (hasNoFilters) {
            return storiesByStatus
        }

        return storiesByStatus.mapValues { (_, stories) ->
            stories.filter { story ->
                val userStory = story.userStory

                if (selectedAssigneeIds.isNotEmpty() || includeUnassigned) {
                    val assignedIds = userStory.assignedUserIds
                    val hasAssignee = assignedIds.any { selectedAssigneeIds.contains(it) }
                    val isUnassigned = assignedIds.isEmpty()
                    if (!(hasAssignee || (includeUnassigned && isUnassigned))) return@filter false
                }

                if (selectedCreatorIds.isNotEmpty()) {
                    if (!selectedCreatorIds.contains(userStory.creatorId)) return@filter false
                }

                if (selectedTags.isNotEmpty()) {
                    val matchesTag = userStory.tags.any { selectedTags.contains(it.name) }
                    if (!matchesTag) return@filter false
                }

                if (selectedEpicIds.isNotEmpty()) {
                    val matchesEpic = userStory.userStoryEpics.any { selectedEpicIds.contains(it.id) }
                    if (!matchesEpic) return@filter false
                }

                if (selectedRoles.isNotEmpty()) {
                    val matchesRole = story.assignees.any { selectedRoles.contains(it.role) }
                    if (!matchesRole) return@filter false
                }

                true
            }.toImmutableList()
        }.toImmutableMap()
    }
}
