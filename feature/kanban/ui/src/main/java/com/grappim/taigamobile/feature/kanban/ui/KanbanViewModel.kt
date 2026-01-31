package com.grappim.taigamobile.feature.kanban.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.kanban.domain.GetKanbanDataUseCase
import com.grappim.taigamobile.feature.kanban.domain.KanbanUserStory
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
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
    private val userStoriesRepository: UserStoriesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        KanbanState(
            onRefresh = ::refresh,
            onSelectSwimlane = ::selectSwimlane,
            onMoveStory = ::moveStory
        )
    )
    val state = _state.asStateFlow()

    init {
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
            ).onSuccess { result ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        statuses = result.statuses,
                        swimlanes = result.swimlanes,
                        stories = result.stories,
                        teamMembers = result.teamMembers,
                        canAddUserStory = result.canAddUserStory,
                        selectedSwimlane = result.defaultSwimlane,
                        storiesByStatus = result.storiesByStatus
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
            _state.update {
                it.copy(
                    selectedSwimlane = swimlane,
                    storiesByStatus = newStoriesByStatus
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
}
