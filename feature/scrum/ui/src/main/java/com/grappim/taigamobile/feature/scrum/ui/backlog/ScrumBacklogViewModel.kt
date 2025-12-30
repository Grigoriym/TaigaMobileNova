package com.grappim.taigamobile.feature.scrum.ui.backlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.utils.ui.NativeText
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
class ScrumBacklogViewModel @Inject constructor(
    private val session: Session,
    private val userStoriesRepository: UserStoriesRepository,
    private val filtersRepository: FiltersRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        ScrumBacklogState(
            onSelectFilters = ::selectFilters,
            retryLoadFilters = ::retryLoadFilters,
            onSetSearchQuery = ::setSearchQuery
        )
    )
    val state = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val userStories: Flow<PagingData<WorkItem>> = combine(
        session.scrumFilters,
        searchQuery
    ) { filters, searchQuery ->
        Pair(filters, searchQuery)
    }.flatMapLatest { (filters, searchQuery) ->
        userStoriesRepository.getUserStoriesPaging(filters = filters, query = searchQuery)
    }.cachedIn(viewModelScope)

    init {
        loadFiltersData()

        session.scrumFilters.onEach { filters ->
            _state.update {
                it.copy(activeFilters = filters)
            }
        }.launchIn(viewModelScope)
    }

    private fun setSearchQuery(newQuery: String) {
        _searchQuery.update { newQuery }
    }

    private fun retryLoadFilters() {
        loadFiltersData()
    }

    private fun loadFiltersData() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isFiltersLoading = true,
                    filtersError = NativeText.Empty
                )
            }

            resultOf {
                filtersRepository.getFiltersData(
                    commonTaskType = CommonTaskType.UserStory,
                    isCommonTaskFromBacklog = true
                )
            }.onSuccess { result ->
                _state.update {
                    it.copy(
                        isFiltersLoading = false,
                        filters = result
                    )
                }

                session.changeScrumFilters(
                    filters = _state.value.activeFilters.updateData(result)
                )
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

    private fun selectFilters(filters: FiltersData) {
        session.changeScrumFilters(filters)
    }
}

data class ScrumBacklogState(
    val activeFilters: FiltersData = FiltersData(),
    val onSelectFilters: (filters: FiltersData) -> Unit = {},
    val filters: FiltersData = FiltersData(),
    val onSetSearchQuery: (String) -> Unit = {},
    val retryLoadFilters: () -> Unit = {},
    val isFiltersLoading: Boolean = false,
    val filtersError: NativeText = NativeText.Empty
)
