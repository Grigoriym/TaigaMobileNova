package io.eugenethedev.taigamobile.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import io.eugenethedev.taigamobile.domain.entities.Project
import io.eugenethedev.taigamobile.domain.entities.Stats
import io.eugenethedev.taigamobile.domain.entities.User
import io.eugenethedev.taigamobile.domain.repositories.IProjectsRepository
import io.eugenethedev.taigamobile.domain.repositories.IUsersRepository
import io.eugenethedev.taigamobile.state.Session
import io.eugenethedev.taigamobile.ui.utils.MutableResultFlow
import io.eugenethedev.taigamobile.ui.utils.loadOrError
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val usersRepository: IUsersRepository,
    private val projectsRepository: IProjectsRepository,
    private val session: Session,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val navRoute = savedStateHandle.toRoute<ProfileNavDestination>()
    private val userId: Long
        get() = navRoute.userId

    val currentUser = MutableResultFlow<User>()
    val currentUserStats = MutableResultFlow<Stats>()
    val currentUserProjects = MutableResultFlow<List<Project>>()
    val currentProjectId by lazy { session.currentProjectId }


    fun onOpen() = viewModelScope.launch {
        currentUser.loadOrError { usersRepository.getUser(userId) }
        currentUserStats.loadOrError { usersRepository.getUserStats(userId) }
        currentUserProjects.loadOrError { projectsRepository.getUserProjects(userId) }
    }
}
