package com.grappim.taigamobile.feature.teams.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TeamViewModel @Inject constructor(private val usersRepository: UsersRepository, taigaStorage: TaigaStorage) :
    ViewModel() {

    private val _state = MutableStateFlow(
        TeamState(
            onRefresh = ::refresh
        )
    )
    val state = _state.asStateFlow()

    init {
        taigaStorage
            .currentProjectIdFlow
            .distinctUntilChanged()
            .onEach { fetchTeam() }
            .launchIn(viewModelScope)
    }

    private suspend fun fetchTeam() {
        _state.update {
            it.copy(
                isLoading = true,
                error = NativeText.Empty
            )
        }
        resultOf {
            usersRepository.getTeamMembers(generateMemberStats = true)
        }.onSuccess { result ->
            _state.update {
                it.copy(
                    isLoading = false,
                    teamMembers = result
                )
            }
        }.onFailure { error ->
            Timber.e(error)
            _state.update {
                it.copy(
                    isLoading = false,
                    error = getErrorMessage(error)
                )
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            fetchTeam()
        }
    }
}
