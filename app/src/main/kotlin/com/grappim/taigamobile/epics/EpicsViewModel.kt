package com.grappim.taigamobile.epics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.grappim.taigamobile.domain.entities.CommonTask
import com.grappim.taigamobile.domain.entities.CommonTaskType
import com.grappim.taigamobile.domain.entities.FiltersData
import com.grappim.taigamobile.domain.repositories.ITasksRepository
import com.grappim.taigamobile.state.Session
import com.grappim.taigamobile.ui.utils.MutableResultFlow
import com.grappim.taigamobile.ui.utils.loadOrError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EpicsViewModel @Inject constructor(
    private val session: Session,
    private val tasksRepository: ITasksRepository,
    private val epicsRepository: EpicsRepository
) : ViewModel() {

    private var shouldReload = true

    val filters = MutableResultFlow<FiltersData>()
    val activeFilters by lazy { session.epicsFilters }

    val epics: Flow<PagingData<CommonTask>> = activeFilters.flatMapLatest { filters ->
        epicsRepository.getEpics(filters)
    }.cachedIn(viewModelScope)

    // TODO handle refresh
    init {
        session.currentProjectId.onEach {
//            epics.refresh()
            shouldReload = true
        }.launchIn(viewModelScope)

        session.taskEdit.onEach {
//            epics.refresh()
        }.launchIn(viewModelScope)
    }

    fun onOpen() {
        if (!shouldReload) return
        viewModelScope.launch {
            filters.loadOrError { tasksRepository.getFiltersData(CommonTaskType.Epic) }
            filters.value.data?.let {
                session.changeEpicsFilters(activeFilters.value.updateData(it))
            }
        }
        shouldReload = false
    }

    fun selectFilters(filters: FiltersData) {
        session.changeEpicsFilters(filters)
    }
}
