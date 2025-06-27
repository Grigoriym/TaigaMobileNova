package com.grappim.taigamobile.feature.projectselector.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.grappim.taigamobile.core.domain.Project
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProjectSelectorViewModel @Inject constructor(
    private val projectsRepository: ProjectsRepository,
    private val session: Session,
    private val taigaStorage: TaigaStorage,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route = savedStateHandle.toRoute<ProjectSelectorNavDestination>()

    private val _state = MutableStateFlow(
        ProjectSelectorState(
            isFromLogin = route.isFromLogin,
            setProjectsQuery = ::searchProjects
        )
    )
    val state = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val projects: Flow<PagingData<Project>> = _searchQuery.flatMapLatest { query ->
        projectsRepository.fetchProjects(query)
    }.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            launch {
                taigaStorage.currentProjectIdFlow.collect { id ->
                    _state.update {
                        it.copy(currentProjectId = id)
                    }
                }
            }
        }
    }

    fun searchProjects(query: String) {
        _searchQuery.value = query
    }

    fun selectProject(project: Project) {
        viewModelScope.launch {
            taigaStorage.setCurrentProjectId(projectId = project.id)
            session.changeCurrentProjectName(project.name)
        }
    }
}
