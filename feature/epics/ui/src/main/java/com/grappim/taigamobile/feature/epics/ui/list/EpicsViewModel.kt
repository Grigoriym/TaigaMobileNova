package com.grappim.taigamobile.feature.epics.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EpicsViewModel @Inject constructor(
    private val session: Session,
    private val epicsRepository: EpicsRepository,
    private val filtersRepository: FiltersRepository,
    taigaStorage: TaigaStorage
) : ViewModel() {

    private val _state = MutableStateFlow(
        EpicsState(
            onRefresh = ::refresh
        )
    )
    val state = _state.asStateFlow()

    val epics: Flow<PagingData<CommonTask>> = session.epicsFilters.onEach { filters ->
        _state.update { it.copy(activeFilters = filters) }
    }.flatMapLatest { filters ->
        epicsRepository.getEpicsPaging(filters)
    }.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            launch {
                taigaStorage.currentProjectIdFlow.distinctUntilChanged().collect {
                    refresh()
                }
            }
            launch {
                session.taskEdit.onEach {
                    refresh()
                }.launchIn(viewModelScope)
            }
            launch {
                loadFilters()
            }
        }
    }

    private suspend fun loadFilters() {
        filtersRepository.getFiltersDataResultOld(CommonTaskType.Epic)
            .onSuccess { data ->
                _state.update {
                    it.copy(
                        isError = false,
                        filters = data
                    )
                }
                val updatedActiveFilters = _state.value.activeFilters.updateData(data)
                session.changeEpicsFilters(updatedActiveFilters)
            }.onFailure {
                _state.update { it.copy(isError = true) }
            }
    }

    private fun refresh() {
        epicsRepository.refreshEpics()
    }

    fun selectFilters(filters: FiltersDataDTO) {
        session.changeEpicsFilters(filters)
    }
}
