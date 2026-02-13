package com.grappim.taigamobile.feature.teams.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.logger.logcat
import com.grappim.taigamobile.core.storage.KmpTaigaSessionStorage
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class TeamViewModel(
    private val usersRepository: UsersRepository,
    taigaSessionStorage: KmpTaigaSessionStorage
) : ViewModel() {

    private val _state = MutableStateFlow(
        TeamState(
            onRefresh = ::refresh
        )
    )
    val state = _state.asStateFlow()

    init {
        taigaSessionStorage
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
            logcat(throwable = error) {
                "Error fetching team members"
            }
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
