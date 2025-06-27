package com.grappim.taigamobile.feature.issues.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersData
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class IssuesViewModel @Inject constructor(
    private val session: Session,
    private val issuesRepository: IssuesRepository,
    private val filtersRepository: FiltersRepository,
    taigaStorage: TaigaStorage
) : ViewModel() {

    private val _state = MutableStateFlow(IssuesState())
    val state = _state.asStateFlow()

    val issues = session.issuesFilters.flatMapLatest { filters ->
        _state.update {
            it.copy(activeFilters = filters)
        }
        issuesRepository.getIssuesPaging(filters)
    }.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            combine(taigaStorage.currentProjectIdFlow, session.taskEdit) {
                issuesRepository.refreshIssues()
            }.launchIn(viewModelScope)

            launch {
                filtersRepository.getFiltersDataResult(CommonTaskType.Issue)
                    .onSuccess { filters ->
                        session.changeIssuesFilters(
                            filters = _state.value.activeFilters.updateData(filters)
                        )
                        _state.update {
                            it.copy(
                                filters = filters,
                                isFiltersError = false
                            )
                        }
                    }.onFailure {
                        _state.update {
                            it.copy(isFiltersError = true)
                        }
                    }
            }
        }
    }

    fun selectFilters(filters: FiltersData) {
        session.changeIssuesFilters(filters)
    }
}
