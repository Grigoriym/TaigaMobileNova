package com.grappim.taigamobile.feature.kanban.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.kanban.domain.GetKanbanDataUseCase
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val taigaSessionStorage: TaigaSessionStorage
) : ViewModel() {

    private val _state = MutableStateFlow(
        KanbanState(
            onRefresh = ::refresh,
            onSelectSwimlane = ::selectSwimlane
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
}
