package com.grappim.taigamobile.feature.epics.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.delegates.SnackbarDelegate
import com.grappim.taigamobile.utils.ui.delegates.SnackbarDelegateImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
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
    taigaStorage: TaigaStorage
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
    ) { filters, query ->
        epicsRepository.getEpicsPaging(filters, query)
    }.flatMapLatest { it }
        .cachedIn(viewModelScope)

    private val retryFiltersSignal = MutableSharedFlow<Unit>()

    val filters = merge(
        taigaStorage.currentProjectIdFlow.distinctUntilChanged(),
        retryFiltersSignal
    ).mapLatest {
        loadFiltersData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FiltersData()
    )

    init {
        taigaStorage.currentProjectIdFlow
            .distinctUntilChanged()
            .onEach {
                refresh()
            }.launchIn(viewModelScope)

        session.epicsFilters
            .onEach { filters ->
                _state.update {
                    it.copy(activeFilters = filters)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun retryLoadFilters() {
        viewModelScope.launch {
            retryFiltersSignal.emit(Unit)
        }
    }

    private suspend fun loadFiltersData(): FiltersData {
        _state.update {
            it.copy(
                isFiltersLoading = true,
                isFiltersError = false
            )
        }

        val result = resultOf { filtersRepository.getFiltersData(CommonTaskType.Epic) }
        return if (result.isSuccess) {
            val filters = result.getOrThrow()
            session.changeEpicsFilters(
                filters = _state.value.activeFilters.updateData(filters)
            )

            _state.update {
                it.copy(
                    isFiltersLoading = false,
                    isFiltersError = false
                )
            }

            filters
        } else {
            Timber.e(result.exceptionOrNull())
            _state.update {
                it.copy(
                    isFiltersLoading = false,
                    isFiltersError = true
                )
            }
            showSnackbarSuspend(NativeText.Resource(RString.filters_loading_error))

            FiltersData()
        }
    }

    private fun setSearchQuery(newQuery: String) {
        _searchQuery.update {
            newQuery
        }
    }

    private fun refresh() {
        epicsRepository.refreshEpics()
    }

    private fun selectFilters(filters: FiltersData) {
        session.changeEpicsFilters(filters)
    }
}
