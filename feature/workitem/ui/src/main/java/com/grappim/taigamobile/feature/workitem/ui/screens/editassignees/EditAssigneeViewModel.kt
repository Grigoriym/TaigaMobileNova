package com.grappim.taigamobile.feature.workitem.ui.screens.editassignees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.ui.models.TeamMemberUI
import com.grappim.taigamobile.feature.workitem.ui.models.TeamMemberUIMapper
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditShared
import dagger.hilt.android.lifecycle.HiltViewModel
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
            selectedTeamMemberId = editShared.currentAssignee,
            originalTeamMemberId = editShared.currentAssignee,
            onTeamMemberClick = ::onTeamMemberClick,
            setIsDialogVisible = ::setIsDialogVisible,
            wasAssigneeChanged = ::wasAssigneeChanged
        )
    )
    val state = _state.asStateFlow()

    init {
        getUsers()
    }

    private fun wasAssigneeChanged(shouldReturnCurrentValue: Boolean): Boolean {
        val wasAssigneeChanged =
            _state.value.selectedTeamMemberId != _state.value.originalTeamMemberId
        if (shouldReturnCurrentValue && wasAssigneeChanged) {
            editShared.setCurrentAssignee(_state.value.selectedTeamMemberId)
            return true
        }
        return false
    }

    private fun setIsDialogVisible(newValue: Boolean) {
        _state.update {
            it.copy(isDialogVisible = newValue)
        }
    }

    private fun onTeamMemberClick(teamMemberUI: TeamMemberUI) {
        val clickedTheSame = teamMemberUI.id == _state.value.selectedTeamMemberId
        _state.update {
            it.copy(
                selectedTeamMemberId = if (clickedTheSame) {
                    null
                } else {
                    teamMemberUI.id
                }
            )
        }
    }

    private fun getUsers() {
        viewModelScope.launch {
            usersRepository.getCurrentTeamResult()
                .onSuccess { result ->
                    val teamMembers = teamMemberUIMapper.toUI(result)
                    _state.update {
                        it.copy(assignees = teamMembers)
                    }
                }.onFailure { error ->
                    Timber.e(error)
                }
        }
    }
}
