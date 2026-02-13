package com.grappim.taigamobile.feature.kanban.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.kanban.domain.GetKanbanDataUseCase
import com.grappim.taigamobile.feature.kanban.domain.KanbanUserStory
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
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
    private val filtersRepository: FiltersRepository
) : ViewModel() {

    private var allFilters = FiltersData()

    private val _state = MutableStateFlow(
        KanbanState(
            onRefresh = ::refresh,
            onSelectSwimlane = ::selectSwimlane,
            onSelectFilters = ::selectFilters,
            onRetryFilters = ::loadFiltersData,
            onMoveStory = ::moveStory
        )
    )
    val state = _state.asStateFlow()

    init {
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
                val activeFilters = _state.value.filtersBySwimlane[result.defaultSwimlane?.id]
                    ?: FiltersData()
                val unfiltered = getKanbanDataUseCase.computeStoriesByStatus(
                    stories = result.stories,
                    statuses = result.statuses,
                    teamMembers = result.teamMembers,
                    swimlane = result.defaultSwimlane
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
                        unfilteredStoriesByStatus = unfiltered,
                        storiesByStatus = filterStories(unfiltered, activeFilters),
                        filters = computeSwimlaneFilters(unfiltered, result.teamMembers),
                        activeFilters = activeFilters
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
                    allFilters = result
                    val currentState = _state.value

                    _state.update {
                        it.copy(
                            isFiltersLoading = false,
                            filters = computeSwimlaneFilters(
                                currentState.unfilteredStoriesByStatus,
                                currentState.teamMembers
                            )
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

            val activeFilters = currentState.filtersBySwimlane[swimlane?.id]
                ?: FiltersData()

            val unfiltered = getKanbanDataUseCase.computeStoriesByStatus(
                stories = currentState.stories,
                statuses = currentState.statuses,
                teamMembers = currentState.teamMembers,
                swimlane = swimlane
            )

            val updatedFiltersBySwimlane = currentState.filtersBySwimlane
                .toMutableMap()
                .apply { put(swimlane?.id, activeFilters) }
                .toPersistentMap()

            _state.update {
                it.copy(
                    selectedSwimlane = swimlane,
                    unfilteredStoriesByStatus = unfiltered,
                    storiesByStatus = filterStories(unfiltered, activeFilters),
                    activeFilters = activeFilters,
                    filters = computeSwimlaneFilters(unfiltered, currentState.teamMembers),
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
        val currentState = _state.value

        val updatedFiltersBySwimlane = currentState.filtersBySwimlane
            .toMutableMap()
            .apply { put(currentState.selectedSwimlane?.id, filters) }
            .toPersistentMap()

        _state.update {
            it.copy(
                activeFilters = filters,
                storiesByStatus = filterStories(currentState.unfilteredStoriesByStatus, filters),
                filtersBySwimlane = updatedFiltersBySwimlane
            )
        }
    }

    private fun computeSwimlaneFilters(
        storiesByStatus: ImmutableMap<Statuses, ImmutableList<KanbanUserStory>>,
        teamMembers: ImmutableList<TeamMember>
    ): FiltersData {
        if (allFilters.filtersNumber == 0) return FiltersData()

        val boardStories = storiesByStatus.values.flatten()

        val assigneeCounts = mutableMapOf<Long?, Long>()
        val creatorCounts = mutableMapOf<Long, Long>()
        val statusCounts = mutableMapOf<Long, Long>()
        val tagCounts = mutableMapOf<String, Long>()
        val epicCounts = mutableMapOf<Long, Long>()
        for (kanbanStory in boardStories) {
            val story = kanbanStory.userStory
            if (story.assignedUserIds.isEmpty()) {
                assigneeCounts[null] = (assigneeCounts[null] ?: 0) + 1
            }
            for (id in story.assignedUserIds) {
                assigneeCounts[id] = (assigneeCounts[id] ?: 0) + 1
            }
            creatorCounts[story.creatorId] = (creatorCounts[story.creatorId] ?: 0) + 1
            story.status?.id?.let { statusId ->
                statusCounts[statusId] = (statusCounts[statusId] ?: 0) + 1
            }
            for (tag in story.tags) {
                tagCounts[tag.name] = (tagCounts[tag.name] ?: 0) + 1
            }
            for (epic in story.userStoryEpics) {
                epicCounts[epic.id] = (epicCounts[epic.id] ?: 0) + 1
            }
        }

        val teamMemberById = teamMembers.associateBy { it.id }
        val roleCounts = mutableMapOf<String, Long>()
        for ((id, count) in assigneeCounts) {
            if (id != null) {
                val role = teamMemberById[id]?.role ?: continue
                roleCounts[role] = (roleCounts[role] ?: 0) + count
            }
        }

        return allFilters.copy(
            assignees = allFilters.assignees.mapNotNull { filter ->
                val count = assigneeCounts[filter.id] ?: return@mapNotNull null
                filter.copy(count = count)
            }.toImmutableList(),
            createdBy = allFilters.createdBy.mapNotNull { filter ->
                val count = filter.id?.let { creatorCounts[it] } ?: return@mapNotNull null
                filter.copy(count = count)
            }.toImmutableList(),
            statuses = allFilters.statuses.mapNotNull { filter ->
                val count = statusCounts[filter.id] ?: return@mapNotNull null
                filter.copy(count = count)
            }.toImmutableList(),
            tags = allFilters.tags.mapNotNull { filter ->
                val count = tagCounts[filter.name] ?: return@mapNotNull null
                filter.copy(count = count)
            }.toImmutableList(),
            epics = allFilters.epics.mapNotNull { filter ->
                val count = filter.id?.let { epicCounts[it] } ?: return@mapNotNull null
                filter.copy(count = count)
            }.toImmutableList(),
            roles = allFilters.roles.mapNotNull { filter ->
                val count = roleCounts[filter.name] ?: return@mapNotNull null
                filter.copy(count = count)
            }.toImmutableList()
        )
    }

    private fun filterStories(
        storiesByStatus: ImmutableMap<Statuses, ImmutableList<KanbanUserStory>>,
        activeFilters: FiltersData
    ): ImmutableMap<Statuses, ImmutableList<KanbanUserStory>> {
        val selectedAssigneeIds = activeFilters.assignees.mapNotNull { it.id }.toSet()
        val includeUnassigned = activeFilters.assignees.any { it.id == null }

        val selectedCreatorIds = activeFilters.createdBy.mapNotNull { it.id }.toSet()
        val selectedStatusIds = activeFilters.statuses.map { it.id }.toSet()
        val selectedTags = activeFilters.tags.map { it.name }.toSet()
        val selectedEpicIds = activeFilters.epics.map { it.id }.toSet()
        val selectedRoles = activeFilters.roles.map { it.name }.toSet()

        val hasNoFilters = selectedAssigneeIds.isEmpty() && !includeUnassigned &&
            selectedCreatorIds.isEmpty() &&
            selectedStatusIds.isEmpty() &&
            selectedTags.isEmpty() &&
            selectedEpicIds.isEmpty() &&
            selectedRoles.isEmpty()

        if (hasNoFilters) {
            return storiesByStatus
        }

        fun KanbanUserStory.matchesAssignees(): Boolean {
            if (selectedAssigneeIds.isEmpty() && !includeUnassigned) return true

            val assignedIds = userStory.assignedUserIds
            val hasAssignee = assignedIds.any { selectedAssigneeIds.contains(it) }
            val isUnassigned = assignedIds.isEmpty()
            return hasAssignee || (includeUnassigned && isUnassigned)
        }

        fun KanbanUserStory.matchesCreators(): Boolean {
            if (selectedCreatorIds.isEmpty()) return true
            return selectedCreatorIds.contains(userStory.creatorId)
        }

        fun KanbanUserStory.matchesStatuses(): Boolean {
            if (selectedStatusIds.isEmpty()) return true
            return userStory.status?.id?.let { selectedStatusIds.contains(it) } ?: false
        }

        fun KanbanUserStory.matchesTags(): Boolean {
            if (selectedTags.isEmpty()) return true
            return userStory.tags.any { selectedTags.contains(it.name) }
        }

        fun KanbanUserStory.matchesEpics(): Boolean {
            if (selectedEpicIds.isEmpty()) return true
            return userStory.userStoryEpics.any { selectedEpicIds.contains(it.id) }
        }

        fun KanbanUserStory.matchesRoles(): Boolean {
            if (selectedRoles.isEmpty()) return true
            return assignees.any { selectedRoles.contains(it.role) }
        }

        return storiesByStatus.mapValues { (_, stories) ->
            stories.filter { story ->
                story.matchesAssignees() &&
                    story.matchesCreators() &&
                    story.matchesStatuses() &&
                    story.matchesTags() &&
                    story.matchesEpics() &&
                    story.matchesRoles()
            }.toImmutableList()
        }.toImmutableMap()
    }
}
