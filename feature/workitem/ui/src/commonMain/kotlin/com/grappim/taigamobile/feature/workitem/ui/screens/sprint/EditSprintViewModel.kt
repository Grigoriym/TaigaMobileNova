package com.grappim.taigamobile.feature.workitem.ui.screens.sprint

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.logger.logcat
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.projects.domain.canModifyIssue
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.utils.ui.typeMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.reflect.typeOf

@KoinViewModel
class EditSprintViewModel(
    private val sprintsRepository: SprintsRepository,
    private val workItemEditStateRepository: WorkItemEditStateRepository,
    private val projectsRepository: ProjectsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route = savedStateHandle.toRoute<WorkItemEditSprintNavDestination>(
        typeMap = typeMapOf(
            listOf(
                typeOf<TaskIdentifier>()
            )
        )
    )

    private val _state = MutableStateFlow(
        EditSprintState(
            onSprintClick = ::onSprintClick,
            setIsDialogVisible = ::setIsDialogVisible,
            isItemSelected = ::isItemSelected,
            shouldGoBackWithCurrentValue = ::onGoingBack
        )
    )
    val state = _state.asStateFlow()

    private val _onBackAction = Channel<Unit>()
    val onBackAction = _onBackAction.receiveAsFlow()

    init {
        getSprints()
        getPermissions()
    }

    private fun getPermissions() {
        viewModelScope.launch {
            val canModify = when (route.taskIdentifier) {
                is TaskIdentifier.WorkItem -> {
                    val taskType = route.taskIdentifier.commonTaskType
                    when (taskType) {
                        CommonTaskType.Issue -> {
                            projectsRepository.getPermissions().canModifyIssue()
                        }

                        else -> true
                    }
                }

                else -> true
            }

            _state.update {
                it.copy(canModify = canModify)
            }
        }
    }

    private fun onGoingBack(shouldReturnCurrentValue: Boolean) {
        viewModelScope.launch {
            setIsDialogVisible(false)
            notifyChange(shouldReturnCurrentValue)
            _onBackAction.send(Unit)
        }
    }

    private fun isItemSelected(id: Long): Boolean = id == state.value.selectedItem

    private fun notifyChange(shouldReturnCurrentValue: Boolean) {
        viewModelScope.launch {
            val wasStateChanged = _state.value.selectedItem != _state.value.originalSelectedItem
            if (shouldReturnCurrentValue && wasStateChanged) {
                workItemEditStateRepository.updateSprint(
                    workItemId = route.workItemId,
                    type = route.taskIdentifier,
                    id = _state.value.selectedItem
                )
            }
        }
    }

    private fun setIsDialogVisible(newValue: Boolean) {
        _state.update {
            it.copy(isDialogVisible = newValue)
        }
    }

    private fun onSprintClick(id: Long) {
        val clickedTheSame = id == _state.value.selectedItem
        val newSelection = if (clickedTheSame) null else id

        _state.update {
            it.copy(selectedItem = newSelection)
        }
    }

    private fun getSprints() {
        viewModelScope.launch {
            resultOf { sprintsRepository.getSprints(isClosed = false) }
                .onSuccess { result ->
                    val originalSprint = workItemEditStateRepository.getCurrentSprint(
                        workItemId = route.workItemId,
                        type = route.taskIdentifier
                    )
                    _state.update {
                        it.copy(
                            itemsToShow = result.toPersistentList(),
                            selectedItem = originalSprint,
                            originalSelectedItem = originalSprint
                        )
                    }
                }.onFailure { error ->
                    logcat(throwable = error) {
                        "Error while getting sprints"
                    }
                }
        }
    }
}
