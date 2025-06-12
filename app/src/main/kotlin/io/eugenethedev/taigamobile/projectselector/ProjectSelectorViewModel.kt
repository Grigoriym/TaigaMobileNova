package io.eugenethedev.taigamobile.projectselector

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.Pager
import androidx.paging.PagingConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import io.eugenethedev.taigamobile.domain.entities.Project
import io.eugenethedev.taigamobile.domain.paging.CommonPagingSource
import io.eugenethedev.taigamobile.domain.repositories.IProjectsRepository
import io.eugenethedev.taigamobile.state.Session
import io.eugenethedev.taigamobile.ui.utils.asLazyPagingItems
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class ProjectSelectorViewModel @Inject constructor(
    private val projectsRepository: IProjectsRepository,
    private val session: Session,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val navDestination = savedStateHandle.toRoute<ProjectSelectorNavDestination>()

    val currentProjectId by lazy { session.currentProjectId }

    fun onOpen() {
        projects.refresh()
    }

    private val projectsQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val projects by lazy {
        projectsQuery.flatMapLatest { query ->
            Pager(PagingConfig(CommonPagingSource.PAGE_SIZE)) {
                CommonPagingSource { projectsRepository.searchProjects(query, it) }
            }.flow
        }.asLazyPagingItems(viewModelScope)
    }

    fun searchProjects(query: String) {
        projectsQuery.value = query
    }

    fun selectProject(project: Project) {
        session.changeCurrentProject(project.id, project.name)
    }
}
