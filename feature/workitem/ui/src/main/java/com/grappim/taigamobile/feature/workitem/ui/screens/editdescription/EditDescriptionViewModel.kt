package com.grappim.taigamobile.feature.workitem.ui.screens.editdescription

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.utils.ui.typeMapOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.typeOf

@HiltViewModel
class EditDescriptionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workItemEditStateRepository: WorkItemEditStateRepository
) : ViewModel() {
    private val route = savedStateHandle.toRoute<WorkItemEditDescriptionNavDestination>(
        typeMap = typeMapOf(
            listOf(
                typeOf<TaskIdentifier>()
            )
        )
    )
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
            val wasDescriptionChanged =
                _state.value.currentDescription != _state.value.originalDescription
            if (shouldReturnCurrentValue && wasDescriptionChanged) {
                workItemEditStateRepository.updateDescription(
                    workItemId = route.workItemId,
                    type = route.taskIdentifier,
                    description = _state.value.currentDescription
                )
            }
            _onBackAction.send(Unit)
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
