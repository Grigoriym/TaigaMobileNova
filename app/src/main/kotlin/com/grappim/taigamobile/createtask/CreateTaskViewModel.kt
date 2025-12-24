package com.grappim.taigamobile.createtask

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val createWorkItemUseCase: CreateWorkItemUseCase
) : ViewModel() {

    val route = savedStateHandle.toRoute<CreateTaskNavDestination>()

    private val _state = MutableStateFlow(
        CreateTaskState(
            toolbarTitle = NativeText.Resource(
                when (route.type) {
                    CommonTaskType.UserStory -> RString.create_userstory
                    CommonTaskType.Task -> RString.create_task
                    CommonTaskType.Epic -> RString.create_epic
                    CommonTaskType.Issue -> RString.create_issue
                }
            ),
            setTitle = ::setTitle,
            setDescription = ::setDescription,
            onCreateTask = ::onCreateTask
        )
    )
    val state = _state.asStateFlow()

    private val _creationResult = Channel<CreateWorkItemData>()
    val creationResult = _creationResult.receiveAsFlow()

    private fun onCreateTask() {
        val title = state.value.title.text.trim()
        if (title.isEmpty()) {
            _state.update {
                it.copy(
                    error = NativeText.Resource(RString.title_is_empty)
                )
            }
            return
        }

        _state.update {
            it.copy(
                isLoading = true
            )
        }

        val description = state.value.description.text.trim()

        viewModelScope.launch {
            createWorkItemUseCase.createTask(
                commonTaskType = route.type,
                title = title,
                description = description,
                parentId = route.parentId,
                sprintId = route.sprintId,
                statusId = route.statusId,
                swimlaneId = route.swimlaneId
            ).onSuccess { result ->
                _state.update {
                    it.copy(
                        isLoading = false
                    )
                }
                _creationResult.send(result)
            }.onFailure { error ->
                Timber.e(error)

                _state.update {
                    it.copy(
                        error = getErrorMessage(error),
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun setTitle(title: TextFieldValue) {
        _state.update {
            it.copy(title = title)
        }
    }

    private fun setDescription(description: TextFieldValue) {
        _state.update {
            it.copy(description = description)
        }
    }
}
