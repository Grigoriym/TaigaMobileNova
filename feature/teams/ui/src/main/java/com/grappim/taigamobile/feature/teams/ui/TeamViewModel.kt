package com.grappim.taigamobile.feature.teams.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.TeamMember
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.utils.ui.NothingResult
import com.grappim.taigamobile.utils.ui.loadOrError
import com.grappim.taigamobile.utils.ui.mutableResultFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    session: Session
) : ViewModel() {

    val team = mutableResultFlow<List<TeamMember>?>()

    private var shouldReload = true

    init {
        session.currentProjectId.onEach {
            team.value = NothingResult()
            shouldReload = true
        }.launchIn(viewModelScope)

        onOpen()
    }

    fun onOpen() {
        if (!shouldReload) return
        viewModelScope.launch {
            team.loadOrError { usersRepository.getTeam() }
        }
        shouldReload = false
    }
}
