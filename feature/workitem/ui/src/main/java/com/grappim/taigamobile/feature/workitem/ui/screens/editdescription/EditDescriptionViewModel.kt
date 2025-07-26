package com.grappim.taigamobile.feature.workitem.ui.screens.editdescription

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class EditDescriptionViewModel @Inject constructor(savedStateHandle: SavedStateHandle) :
    ViewModel() {
    private val route = savedStateHandle.toRoute<WorkItemEditDescriptionNavDestination>()
    private val _state = MutableStateFlow(
        EditDescriptionState(
            originalDescription = route.description,
            currentDescription = route.description,
            onDescriptionChange = ::onDescriptionChange,
            setIsDialogVisible = ::setIsDialogVisible,
            retrieveDescriptionToChange = ::retrieveDescriptionToChange
        )
    )
    val state = _state.asStateFlow()

    /**
     * We will return a non-null value only if the description has changed.
     */
    private fun retrieveDescriptionToChange(shouldReturnCurrentValue: Boolean): String? {
        val wasDescriptionChanged =
            _state.value.currentDescription != _state.value.originalDescription
        if (shouldReturnCurrentValue && wasDescriptionChanged) {
            return _state.value.currentDescription
        }

        return null
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
