package com.grappim.taigamobile.feature.workitem.ui.screens.epic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.logger.logcat
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class EditEpicViewModel(
    private val epicsRepository: EpicsRepository,
    private val workItemEditStateRepository: WorkItemEditStateRepository,
    private val taigaSessionStorage: TaigaSessionStorage,
    @InjectedParam private val route: WorkItemEditEpicNavDestination
) : ViewModel() {

    private val _state = MutableStateFlow(
        EditEpicState(
            onEpicClick = ::onEpicClick,
            setIsDialogVisible = ::setIsDialogVisible,
            isItemSelected = ::isItemSelected,
            shouldGoBackWithCurrentValue = ::onGoingBack
        )
    )
    val state = _state.asStateFlow()

    private val _onBackAction = Channel<Unit>()
    val onBackAction = _onBackAction.receiveAsFlow()

    init {
        getEpics()
    }

    private fun onGoingBack(shouldReturnCurrentValue: Boolean) {
        viewModelScope.launch {
            setIsDialogVisible(false)
            notifyChange(shouldReturnCurrentValue)
            _onBackAction.send(Unit)
        }
    }

    private fun isItemSelected(id: Long): Boolean = state.value.selectedItems.contains(id)

    private fun notifyChange(shouldReturnCurrentValue: Boolean) {
        viewModelScope.launch {
            val wasStateChanged = _state.value.selectedItems != _state.value.originalSelectedItems
            if (shouldReturnCurrentValue && wasStateChanged) {
                workItemEditStateRepository.updateEpics(
                    workItemId = route.workItemId,
                    type = route.taskIdentifier,
                    ids = _state.value.selectedItems
                )
            }
        }
    }

    private fun setIsDialogVisible(newValue: Boolean) {
        _state.update {
            it.copy(isDialogVisible = newValue)
        }
    }

    private fun onEpicClick(id: Long) {
        _state.update { currentState ->
            val currentSelection = currentState.selectedItems
            val newSelection = if (currentSelection.contains(id)) {
                currentSelection.remove(id)
            } else {
                currentSelection.add(id)
            }
            currentState.copy(selectedItems = newSelection)
        }
    }

    private fun getEpics() {
        viewModelScope.launch {
            resultOf {
                epicsRepository.getEpics(projectId = taigaSessionStorage.getCurrentProjectId())
            }.onSuccess { result ->
                val selected = workItemEditStateRepository.getCurrentEpics(
                    workItemId = route.workItemId,
                    type = route.taskIdentifier
                ).toPersistentList()
                _state.update {
                    it.copy(
                        itemsToShow = result.toPersistentList(),
                        selectedItems = selected,
                        originalSelectedItems = selected
                    )
                }
            }.onFailure { error ->
                logcat(throwable = error) {
                    "Error while getting epics"
                }
            }
        }
    }
}
