package com.grappim.taigamobile.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.Project
import com.grappim.taigamobile.core.domain.TasksRepository
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.utils.ui.NothingResult
import com.grappim.taigamobile.utils.ui.loadOrError
import com.grappim.taigamobile.utils.ui.mutableResultFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val projectsRepository: ProjectsRepository,
    private val session: Session
) : ViewModel() {

    val workingOn = mutableResultFlow<List<CommonTask>>()
    val watching = mutableResultFlow<List<CommonTask>>()
    val myProjects = mutableResultFlow<List<Project>>()

    val currentProjectId by lazy { session.currentProjectId }

    private var shouldReload = true

    init {
        session.taskEdit.onEach {
            workingOn.value = NothingResult()
            watching.value = NothingResult()
            myProjects.value = NothingResult()
            shouldReload = true
        }.launchIn(viewModelScope)

        onOpen()
    }

    fun onOpen() {
        viewModelScope.launch {
            if (!shouldReload) return@launch
            joinAll(
                launch {
                    workingOn.loadOrError(
                        preserveValue = false
                    ) { tasksRepository.getWorkingOn() }
                },
                launch {
                    watching.loadOrError(
                        preserveValue = false
                    ) { tasksRepository.getWatching() }
                },
                launch {
                    myProjects.loadOrError(
                        preserveValue = false
                    ) { projectsRepository.getMyProjects() }
                }
            )
            shouldReload = false
        }
    }

    fun changeCurrentProject(project: Project) {
        project.apply {
            session.changeCurrentProject(id, name)
        }
    }
}
