package com.grappim.taigamobile.createtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.logger.logcat
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.create_epic
import com.grappim.taigamobile.strings.generated.resources.create_issue
import com.grappim.taigamobile.strings.generated.resources.create_task
import com.grappim.taigamobile.strings.generated.resources.create_userstory
import com.grappim.taigamobile.strings.generated.resources.title_is_empty
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class CreateTaskViewModel(
    @InjectedParam val route: CreateTaskNavDestination,
    private val createWorkItemUseCase: CreateWorkItemUseCase
) : ViewModel() {

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
        val title = state.value.title.trim()
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

        val description = state.value.description.trim()

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
                logcat(throwable = error) {
                    "Error creating task"
                }
                _state.update {
                    it.copy(
                        error = getErrorMessage(error),
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun setTitle(title: String) {
        _state.update {
            it.copy(title = title)
        }
    }

    private fun setDescription(description: String) {
        _state.update {
            it.copy(description = description)
        }
    }
}
