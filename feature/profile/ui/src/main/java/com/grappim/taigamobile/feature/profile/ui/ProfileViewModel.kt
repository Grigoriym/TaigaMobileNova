package com.grappim.taigamobile.feature.profile.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.domain.Project
import com.grappim.taigamobile.core.domain.Stats
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.utils.ui.loadOrError
import com.grappim.taigamobile.utils.ui.mutableResultFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val projectsRepository: ProjectsRepository,
    taigaStorage: TaigaStorage,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val navRoute = savedStateHandle.toRoute<ProfileNavDestination>()
    private val userId: Long
        get() = navRoute.userId

    val currentUser = mutableResultFlow<User>()
    val currentUserStats = mutableResultFlow<Stats>()
    val currentUserProjects = mutableResultFlow<List<Project>>()
    val currentProjectId = taigaStorage.currentProjectIdFlow
        .stateIn(
            scope = viewModelScope,
            initialValue = -1,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    fun onOpen() = viewModelScope.launch {
        currentUser.loadOrError { usersRepository.getUser(userId) }
        currentUserStats.loadOrError { usersRepository.getUserStats(userId) }
        currentUserProjects.loadOrError { projectsRepository.getUserProjects(userId) }
    }
}
