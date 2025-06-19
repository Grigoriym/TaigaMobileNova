package com.grappim.taigamobile.issues

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersData
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.subscribeToAll
import com.grappim.taigamobile.domain.repositories.ITasksRepository
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import com.grappim.taigamobile.ui.utils.loadOrError
import com.grappim.taigamobile.ui.utils.mutableResultFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class IssuesViewModel @Inject constructor(
    private val session: Session,
    private val tasksRepository: ITasksRepository,
    private val issuesRepository: IssuesRepository
) : ViewModel() {
    private var shouldReload = true

    val filters = mutableResultFlow<FiltersData>()
    val activeFilters by lazy { session.issuesFilters }

    val issues = activeFilters.flatMapLatest { filters ->
        issuesRepository.getIssues(filters)
    }.cachedIn(viewModelScope)

    init {
        session.currentProjectId.onEach {
            shouldReload = true
        }.launchIn(viewModelScope)

        viewModelScope.subscribeToAll(session.currentProjectId, session.taskEdit) {
//            issues.refresh()
        }
        onOpen()
    }

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

    fun selectFilters(filters: FiltersData) {
        session.changeIssuesFilters(filters)
    }
}
