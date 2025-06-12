package io.eugenethedev.taigamobile.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.eugenethedev.taigamobile.domain.entities.CommonTask
import io.eugenethedev.taigamobile.domain.entities.Project
import io.eugenethedev.taigamobile.domain.repositories.IProjectsRepository
import io.eugenethedev.taigamobile.domain.repositories.ITasksRepository
import io.eugenethedev.taigamobile.state.Session
import io.eugenethedev.taigamobile.ui.utils.MutableResultFlow
import io.eugenethedev.taigamobile.ui.utils.NothingResult
import io.eugenethedev.taigamobile.ui.utils.loadOrError
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val tasksRepository: ITasksRepository,
    private val projectsRepository: IProjectsRepository,
    private val session: Session
) : ViewModel() {

    val workingOn = MutableResultFlow<List<CommonTask>>()
    val watching = MutableResultFlow<List<CommonTask>>()
    val myProjects = MutableResultFlow<List<Project>>()

    val currentProjectId by lazy { session.currentProjectId }

    private var shouldReload = true

    fun onOpen() = viewModelScope.launch {
        if (!shouldReload) return@launch
        joinAll(
            launch { workingOn.loadOrError(preserveValue = false) { tasksRepository.getWorkingOn() } },
            launch { watching.loadOrError(preserveValue = false) { tasksRepository.getWatching() } },
            launch { myProjects.loadOrError(preserveValue = false) { projectsRepository.getMyProjects() } }
        )
        shouldReload = false
    }

    fun changeCurrentProject(project: Project) {
        project.apply {
            session.changeCurrentProject(id, name)
        }
    }

    init {
        session.taskEdit.onEach {
            workingOn.value = NothingResult()
            watching.value = NothingResult()
            myProjects.value = NothingResult()
            shouldReload = true
        }.launchIn(viewModelScope)
    }
}
