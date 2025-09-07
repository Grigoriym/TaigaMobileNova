package com.grappim.taigamobile.feature.workitem.ui.screens.editdescription

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditShared
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditDescriptionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workItemEditShared: WorkItemEditShared
) : ViewModel() {
    private val route = savedStateHandle.toRoute<WorkItemEditDescriptionNavDestination>()
    private val _state = MutableStateFlow(
        EditDescriptionState(
            originalDescription = route.description,
            currentDescription = route.description,
            onDescriptionChange = ::onDescriptionChange,
            setIsDialogVisible = ::setIsDialogVisible,
            shouldGoBackWithCurrentValue = ::onGoingBack
        )
    )
    val state = _state.asStateFlow()

    private val _onBackAction = Channel<Unit>()
    val onBackAction = _onBackAction.receiveAsFlow()

    private fun onGoingBack(shouldReturnCurrentValue: Boolean) {
        viewModelScope.launch {
            setIsDialogVisible(false)
            notifyDescriptionUpdate(shouldReturnCurrentValue)
            _onBackAction.send(Unit)
        }
    }

    /**
     * We will return a non-null value only if the description has changed.
     */
    private fun notifyDescriptionUpdate(shouldReturnCurrentValue: Boolean) {
        val wasDescriptionChanged =
            _state.value.currentDescription != _state.value.originalDescription
        if (shouldReturnCurrentValue && wasDescriptionChanged) {
            workItemEditShared.updateDescription(_state.value.currentDescription)
        }
    }

    private fun onDescriptionChange(newValue: String) {
        _state.update {
            it.copy(currentDescription = newValue)
        }
    }

    private fun setIsDialogVisible(newValue: Boolean) {
        _state.update {
            it.copy(isDialogVisible = newValue)
        }
    }
}
