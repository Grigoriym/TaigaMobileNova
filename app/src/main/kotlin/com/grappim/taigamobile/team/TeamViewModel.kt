package com.grappim.taigamobile.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.TeamMember
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.domain.repositories.IUsersRepository
import com.grappim.taigamobile.ui.utils.NothingResult
import com.grappim.taigamobile.ui.utils.loadOrError
import com.grappim.taigamobile.ui.utils.mutableResultFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val usersRepository: IUsersRepository,
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
