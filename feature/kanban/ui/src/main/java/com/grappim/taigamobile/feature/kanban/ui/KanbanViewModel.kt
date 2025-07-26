package com.grappim.taigamobile.feature.kanban.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.SwimlaneDTO
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.core.storage.subscribeToAll
import com.grappim.taigamobile.feature.kanban.domain.KanbanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KanbanViewModel @Inject constructor(
    session: Session,
    taigaStorage: TaigaStorage,
    private val kanbanRepository: KanbanRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        KanbanState(
            onRefresh = ::refresh,
            onSelectSwimlane = ::selectSwimlane
        )
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                subscribeToAll(taigaStorage.currentProjectIdFlow, session.taskEdit) {
                    _state.update {
                        it.copy(
                            team = emptyList(),
                            statusOlds = emptyList(),
                            stories = emptyList(),
                            swimlaneDTOS = emptyList()
                        )
                    }
                }
            }
            launch {
                getKanbanData()
            }
        }
    }

    private suspend fun getKanbanData() {
        _state.update { it.copy(isLoading = true) }
        kanbanRepository.getData()
            .onSuccess { result ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        team = result.team,
                        statusOlds = result.statusOlds,
                        stories = result.stories,
                        swimlaneDTOS = result.swimlaneDTOS
                    )
                }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error
                    )
                }
            }
    }

    private fun refresh() {
        viewModelScope.launch {
            getKanbanData()
        }
    }

    private fun selectSwimlane(swimlaneDTO: SwimlaneDTO?) {
        _state.update { it.copy(selectedSwimlaneDTO = swimlaneDTO) }
    }
}
