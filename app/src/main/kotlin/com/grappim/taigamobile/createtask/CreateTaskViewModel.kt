package com.grappim.taigamobile.createtask

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.R
import com.grappim.taigamobile.core.ui.NativeText
import com.grappim.taigamobile.domain.entities.CommonTask
import com.grappim.taigamobile.domain.entities.CommonTaskType
import com.grappim.taigamobile.domain.repositories.ITasksRepository
import com.grappim.taigamobile.state.Session
import com.grappim.taigamobile.state.postUpdate
import com.grappim.taigamobile.ui.utils.MutableResultFlow
import com.grappim.taigamobile.ui.utils.loadOrError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val tasksRepository: ITasksRepository,
    private val session: Session,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val route = savedStateHandle.toRoute<CreateTaskNavDestination>()

    private val _state = MutableStateFlow(
        CreateTaskState(
            toolbarTitle = NativeText.Resource(
                when (route.type) {
                    CommonTaskType.UserStory -> R.string.create_userstory
                    CommonTaskType.Task -> R.string.create_task
                    CommonTaskType.Epic -> R.string.create_epic
                    CommonTaskType.Issue -> R.string.create_issue
                }
            ),
            setTitle = ::setTitle,
            setDescription = ::setDescription,
            onCreateTask = ::createTask
        )
    )
    val state = _state.asStateFlow()

    val creationResult = MutableResultFlow<CommonTask>()

    // TODO handle empty title
    private fun createTask() {
        val title = state.value.title.text.trim()
        if (title.isEmpty()) return

        val description = state.value.description.text.trim()

        createTask(
            route.type,
            title,
            description,
            route.parentId,
            route.sprintId,
            route.statusId,
            route.swimlaneId
        )
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

    fun createTask(
        commonTaskType: CommonTaskType,
        title: String,
        description: String,
        parentId: Long? = null,
        sprintId: Long? = null,
        statusId: Long? = null,
        swimlaneId: Long? = null
    ) = viewModelScope.launch {
        creationResult.loadOrError(preserveValue = false) {
            tasksRepository.createCommonTask(
                commonTaskType,
                title,
                description,
                parentId,
                sprintId,
                statusId,
                swimlaneId
            ).also {
                session.taskEdit.postUpdate()
            }
        }
    }
}
