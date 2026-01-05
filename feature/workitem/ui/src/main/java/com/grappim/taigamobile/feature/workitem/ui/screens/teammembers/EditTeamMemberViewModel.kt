package com.grappim.taigamobile.feature.workitem.ui.screens.teammembers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.ui.models.TeamMemberUIMapper
import com.grappim.taigamobile.feature.workitem.ui.screens.TeamMemberEditType
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.utils.ui.typeMapOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.reflect.typeOf

@HiltViewModel
class EditTeamMemberViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val teamMemberUIMapper: TeamMemberUIMapper,
    private val workItemEditStateRepository: WorkItemEditStateRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val route = savedStateHandle.toRoute<WorkItemEditTeamMemberNavDestination>(
        typeMap = typeMapOf(
            listOf(
                typeOf<TaskIdentifier>()
            )
        )
    )

    private val _state = MutableStateFlow(
        EditTeamMemberState(
            selectedItems = retrieveSelectedItems(),
            originalSelectedItems = retrieveSelectedItems(),
            onTeamMemberClick = ::onTeamMemberClick,
            setIsDialogVisible = ::setIsDialogVisible,
            isItemSelected = ::isItemSelected,
            shouldGoBackWithCurrentValue = ::onGoingBack
        )
    )
    val state = _state.asStateFlow()

    private val _onBackAction = Channel<Unit>()
    val onBackAction = _onBackAction.receiveAsFlow()

    init {
        getUsers()
    }

    private fun onGoingBack(shouldReturnCurrentValue: Boolean) {
        viewModelScope.launch {
            setIsDialogVisible(false)
            notifyChange(shouldReturnCurrentValue)
            _onBackAction.send(Unit)
        }
    }

    private fun retrieveSelectedItems(): PersistentList<Long> = when (
        workItemEditStateRepository.getCurrentType(
            workItemId = route.workItemId,
            type = route.taskIdentifier
        )
    ) {
        TeamMemberEditType.Assignee -> {
            val current = workItemEditStateRepository.getCurrentAssignee(
                workItemId = route.workItemId,
                type = route.taskIdentifier
            )

            if (current != null) {
                persistentListOf(current)
            } else {
                persistentListOf()
            }
        }

        TeamMemberEditType.Watchers -> {
            workItemEditStateRepository.getCurrentWatchers(
                workItemId = route.workItemId,
                type = route.taskIdentifier
            ).toPersistentList()
        }

        TeamMemberEditType.Assignees -> {
            workItemEditStateRepository.getCurrentAssignees(
                workItemId = route.workItemId,
                type = route.taskIdentifier
            ).toPersistentList()
        }
    }

    private fun isItemSelected(id: Long): Boolean = id in state.value.selectedItems

    private fun notifyChange(shouldReturnCurrentValue: Boolean) {
        viewModelScope.launch {
            val wasStateChanged = _state.value.selectedItems != _state.value.originalSelectedItems
            if (shouldReturnCurrentValue && wasStateChanged) {
                when (
                    workItemEditStateRepository.getCurrentType(
                        workItemId = route.workItemId,
                        type = route.taskIdentifier
                    )
                ) {
                    TeamMemberEditType.Assignee -> {
                        workItemEditStateRepository.updateAssignee(
                            workItemId = route.workItemId,
                            type = route.taskIdentifier,
                            id = _state.value.selectedItems.firstOrNull()
                        )
                    }

                    TeamMemberEditType.Watchers -> {
                        workItemEditStateRepository.updateWatchers(
                            workItemId = route.workItemId,
                            type = route.taskIdentifier,
                            ids = _state.value.selectedItems
                        )
                    }

                    TeamMemberEditType.Assignees -> {
                        workItemEditStateRepository.updateAssignees(
                            workItemId = route.workItemId,
                            type = route.taskIdentifier,
                            ids = _state.value.selectedItems
                        )
                    }
                }
            }
        }
    }

    private fun setIsDialogVisible(newValue: Boolean) {
        _state.update {
            it.copy(isDialogVisible = newValue)
        }
    }

    private fun onTeamMemberClick(id: Long) {
        val clickedTheSame = id in _state.value.selectedItems
        when (
            workItemEditStateRepository.getCurrentType(
                workItemId = route.workItemId,
                type = route.taskIdentifier
            )
        ) {
            TeamMemberEditType.Assignee -> {
                val newList = if (clickedTheSame) {
                    _state.value.selectedItems.remove(id)
                } else {
                    persistentListOf(id)
                }
                _state.update {
                    it.copy(
                        selectedItems = newList
                    )
                }
            }

            TeamMemberEditType.Watchers -> {
                val newList = if (clickedTheSame) {
                    _state.value.selectedItems.remove(id)
                } else {
                    _state.value.selectedItems.add(id)
                }
                _state.update {
                    it.copy(
                        selectedItems = newList
                    )
                }
            }

            TeamMemberEditType.Assignees -> {
                val newList = if (clickedTheSame) {
                    _state.value.selectedItems.remove(id)
                } else {
                    _state.value.selectedItems.add(id)
                }
                _state.update {
                    it.copy(
                        selectedItems = newList
                    )
                }
            }
        }
    }

    private fun getUsers() {
        viewModelScope.launch {
            when (
                workItemEditStateRepository.getCurrentType(
                    workItemId = route.workItemId,
                    type = route.taskIdentifier
                )
            ) {
                TeamMemberEditType.Assignee -> {
                    resultOf { usersRepository.getTeamMembers() }
                        .onSuccess { result ->
                            val teamMembers = teamMemberUIMapper.toUI(result)
                            _state.update {
                                it.copy(itemsToShow = teamMembers)
                            }
                        }.onFailure { error ->
                            Timber.e(error)
                        }
                }

                TeamMemberEditType.Watchers -> {
                    resultOf { usersRepository.getTeamMembers() }
                        .onSuccess { result ->
                            val teamMembers = teamMemberUIMapper.toUI(result)
                            _state.update {
                                it.copy(itemsToShow = teamMembers)
                            }
                        }.onFailure { error ->
                            Timber.e(error)
                        }
                }

                TeamMemberEditType.Assignees -> {
                    resultOf { usersRepository.getTeamMembers() }
                        .onSuccess { result ->
                            val teamMembers = teamMemberUIMapper.toUI(result)
                            _state.update {
                                it.copy(itemsToShow = teamMembers)
                            }
                        }.onFailure { error ->
                            Timber.e(error)
                        }
                }
            }
        }
    }
}
