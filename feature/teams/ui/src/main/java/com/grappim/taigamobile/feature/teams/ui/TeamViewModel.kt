package com.grappim.taigamobile.feature.teams.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val taigaStorage: TaigaStorage
) : ViewModel() {

    private val _state = MutableStateFlow(
        TeamState(
            onRefresh = ::refresh
        )
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                taigaStorage.currentProjectIdFlow.distinctUntilChanged().collect { projectId ->
                    fetchTeam(projectId)
                }
            }
        }
    }

    private suspend fun fetchTeam(projectId: Long) {
        setIsLoading(true)
        usersRepository.getTeamByProjectId(projectId)
            .onSuccess { result ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        isError = false,
                        teamMembers = result
                    )
                }
            }.onFailure {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isError = true
                    )
                }
            }
    }

    private fun refresh() {
        viewModelScope.launch {
            fetchTeam(taigaStorage.currentProjectIdFlow.first())
        }
    }

    private fun setIsLoading(isLoading: Boolean) {
        _state.update {
            it.copy(isLoading = isLoading)
        }
    }
}
