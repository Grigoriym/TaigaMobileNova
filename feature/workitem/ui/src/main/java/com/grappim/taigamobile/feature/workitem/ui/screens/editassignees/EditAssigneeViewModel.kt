package com.grappim.taigamobile.feature.workitem.ui.screens.editassignees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.ui.models.TeamMemberUIMapper
import com.grappim.taigamobile.feature.workitem.ui.screens.EditType
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditShared
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EditAssigneeViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val teamMemberUIMapper: TeamMemberUIMapper,
    private val editShared: WorkItemEditShared
) : ViewModel() {

    private val _state = MutableStateFlow(
        EditAssigneeState(
            selectedItems = retrieveSelectedItems(),
            originalSelectedItems = retrieveSelectedItems(),
            onTeamMemberClick = ::onTeamMemberClick,
            setIsDialogVisible = ::setIsDialogVisible,
            wasItemChanged = ::wasItemChanged,
            isItemSelected = ::isItemSelected
        )
    )
    val state = _state.asStateFlow()

    init {
        getUsers()
    }

    private fun retrieveSelectedItems(): PersistentList<Long> = when (editShared.currentType) {
        EditType.Assignee -> {
            if (editShared.currentAssignee != null) {
                persistentListOf(editShared.currentAssignee!!)
            } else {
                persistentListOf()
            }
        }

        EditType.Watchers -> {
            editShared.currentWatchers.toPersistentList()
        }

        else -> {
            persistentListOf()
        }
    }

    private fun isItemSelected(id: Long): Boolean = id in state.value.selectedItems

    private fun wasItemChanged(shouldReturnCurrentValue: Boolean): Boolean {
        val wasStateChanged = _state.value.selectedItems != _state.value.originalSelectedItems
        if (shouldReturnCurrentValue && wasStateChanged) {
            when (editShared.currentType) {
                EditType.Assignee -> {
                    editShared.setCurrentAssignee(_state.value.selectedItems.firstOrNull())
                }

                EditType.Watchers -> {
                    editShared.setCurrentWatchers(_state.value.selectedItems)
                }

                else -> {
                }
            }
            return true
        }
        editShared.clear()
        return false
    }

    private fun setIsDialogVisible(newValue: Boolean) {
        _state.update {
            it.copy(isDialogVisible = newValue)
        }
    }

    private fun onTeamMemberClick(id: Long) {
        val clickedTheSame = id in _state.value.selectedItems
        when (editShared.currentType) {
            EditType.Assignee -> {
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

            EditType.Watchers -> {
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

            else -> {
            }
        }
    }

    private fun getUsers() {
        viewModelScope.launch {
            when (editShared.currentType) {
                EditType.Assignee -> {
                    usersRepository.getCurrentTeamResult()
                        .onSuccess { result ->
                            val teamMembers = teamMemberUIMapper.toUI(result)
                            _state.update {
                                it.copy(itemsToShow = teamMembers)
                            }
                        }.onFailure { error ->
                            Timber.e(error)
                        }
                }

                EditType.Watchers -> {
                    usersRepository.getCurrentTeamResult()
                        .onSuccess { result ->
                            val teamMembers = teamMemberUIMapper.toUI(result)
                            _state.update {
                                it.copy(itemsToShow = teamMembers)
                            }
                        }.onFailure { error ->
                            Timber.e(error)
                        }
                }

                else -> {
                }
            }
        }
    }
}
