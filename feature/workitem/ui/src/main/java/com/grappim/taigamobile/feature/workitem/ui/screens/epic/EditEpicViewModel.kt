package com.grappim.taigamobile.feature.workitem.ui.screens.epic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.utils.ui.typeMapOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.reflect.typeOf

@HiltViewModel
class EditEpicViewModel @Inject constructor(
    private val epicsRepository: EpicsRepository,
    private val workItemEditStateRepository: WorkItemEditStateRepository,
    private val taigaStorage: TaigaStorage,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route = savedStateHandle.toRoute<WorkItemEditEpicNavDestination>(
        typeMap = typeMapOf(
            listOf(
                typeOf<TaskIdentifier>()
            )
        )
    )

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
                epicsRepository.getEpics(projectId = taigaStorage.currentProjectIdFlow.first())
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
                Timber.e(error)
            }
        }
    }
}
