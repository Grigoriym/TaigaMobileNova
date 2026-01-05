package com.grappim.taigamobile.feature.epics.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.projects.domain.canAddEpic
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.delegates.SnackbarDelegate
import com.grappim.taigamobile.utils.ui.delegates.SnackbarDelegateImpl
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EpicsViewModel @Inject constructor(
    private val session: Session,
    private val epicsRepository: EpicsRepository,
    private val filtersRepository: FiltersRepository,
    private val projectsRepository: ProjectsRepository
) : ViewModel(),
    SnackbarDelegate by SnackbarDelegateImpl() {

    private val _state = MutableStateFlow(
        EpicsState(
            selectFilters = ::selectFilters,
            retryLoadFilters = ::retryLoadFilters,
            onSetQuery = ::setSearchQuery
        )
    )
    val state = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val epics: Flow<PagingData<WorkItem>> = combine(
        session.epicsFilters,
        searchQuery
    ) { filters, searchQuery ->
        Pair(filters, searchQuery)
    }.flatMapLatest { (filters, searchQuery) ->
        epicsRepository.getEpicsPaging(filters = filters, query = searchQuery)
    }.cachedIn(viewModelScope)

    init {
        loadFiltersData()
        getPermissions()

        session.epicsFilters
            .onEach { filters ->
                _state.update {
                    it.copy(activeFilters = filters)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun retryLoadFilters() {
        loadFiltersData()
    }

    private fun getPermissions() {
        viewModelScope.launch {
            val perm = projectsRepository.getPermissions()
            _state.update {
                it.copy(
                    canAddEpic = perm.canAddEpic()
                )
            }
        }
    }

    private fun loadFiltersData() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isFiltersLoading = true,
                    filtersError = NativeText.Empty
                )
            }

            resultOf { filtersRepository.getFiltersData(CommonTaskType.Epic) }
                .onSuccess { result ->
                    session.changeEpicsFilters(
                        filters = _state.value.activeFilters.updateData(result)
                    )

                    _state.update {
                        it.copy(
                            isFiltersLoading = false,
                            filters = result
                        )
                    }
                }.onFailure { error ->
                    Timber.e(error)
                    _state.update {
                        it.copy(
                            isFiltersLoading = false,
                            filtersError = getErrorMessage(error)
                        )
                    }
                }
        }
    }

    private fun setSearchQuery(newQuery: String) {
        _searchQuery.update {
            newQuery
        }
    }

    private fun selectFilters(filters: FiltersData) {
        session.changeEpicsFilters(filters)
    }
}
