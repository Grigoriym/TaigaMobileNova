package io.eugenethedev.taigamobile.ui.screens.issues

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import io.eugenethedev.taigamobile.domain.entities.CommonTaskType
import io.eugenethedev.taigamobile.domain.entities.FiltersData
import io.eugenethedev.taigamobile.domain.paging.CommonPagingSource
import io.eugenethedev.taigamobile.domain.repositories.ITasksRepository
import io.eugenethedev.taigamobile.state.Session
import io.eugenethedev.taigamobile.state.subscribeToAll
import io.eugenethedev.taigamobile.ui.utils.MutableResultFlow
import io.eugenethedev.taigamobile.ui.utils.asLazyPagingItems
import io.eugenethedev.taigamobile.ui.utils.loadOrError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IssuesViewModel @Inject constructor(
    private val session: Session,
    private val tasksRepository: ITasksRepository
) : ViewModel() {
    val projectName by lazy { session.currentProjectName }

    private var shouldReload = true

    fun onOpen() {
        if (!shouldReload) return
        viewModelScope.launch {
            filters.loadOrError { tasksRepository.getFiltersData(CommonTaskType.Issue) }
            filters.value.data?.let {
                session.changeIssuesFilters(activeFilters.value.updateData(it))
            }
        }
        shouldReload = false
    }

    val filters = MutableResultFlow<FiltersData>()
    val activeFilters by lazy { session.issuesFilters }

    @OptIn(ExperimentalCoroutinesApi::class)
    val issues by lazy {
        activeFilters.flatMapLatest { filters ->
            Pager(PagingConfig(CommonPagingSource.PAGE_SIZE, enablePlaceholders = false)) {
                CommonPagingSource { tasksRepository.getIssues(it, filters) }
            }.flow
        }.asLazyPagingItems(viewModelScope)
    }

    fun selectFilters(filters: FiltersData) {
        session.changeIssuesFilters(filters)
    }

    init {
        session.currentProjectId.onEach {
            shouldReload = true
        }.launchIn(viewModelScope)

        viewModelScope.subscribeToAll(session.currentProjectId, session.taskEdit) {
            issues.refresh()
        }
    }
}
