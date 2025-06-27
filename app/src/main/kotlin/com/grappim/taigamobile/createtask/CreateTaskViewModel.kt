package com.grappim.taigamobile.createtask

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TasksRepositoryOld
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.postUpdate
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.loadOrError
import com.grappim.taigamobile.utils.ui.mutableResultFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val tasksRepositoryOld: TasksRepositoryOld,
    private val session: Session,
    savedStateHandle: SavedStateHandle
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
            onCreateTask = ::createTask
        )
    )
    val state = _state.asStateFlow()

    val creationResult = mutableResultFlow<CommonTask>()

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
            tasksRepositoryOld.createCommonTask(
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
