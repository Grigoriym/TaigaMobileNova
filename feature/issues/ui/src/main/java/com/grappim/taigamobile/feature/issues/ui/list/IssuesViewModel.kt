@file:OptIn(ExperimentalCoroutinesApi::class)

package com.grappim.taigamobile.feature.issues.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.delegates.SnackbarDelegate
import com.grappim.taigamobile.utils.ui.delegates.SnackbarDelegateImpl
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@HiltViewModel
class IssuesViewModel @Inject constructor(
    private val session: Session,
    private val issuesRepository: IssuesRepository,
    private val filtersRepository: FiltersRepository
) : ViewModel(),
    SnackbarDelegate by SnackbarDelegateImpl() {

    private val _state = MutableStateFlow(
        IssuesState(
            selectFilters = ::selectFilters,
            retryLoadFilters = ::retryLoadFilters,
            setSearchQuery = ::setSearchQuery
        )
    )
    val state = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val issues = combine(session.issuesFilters, searchQuery) { filters, searchQuery ->
        Pair(filters, searchQuery)
    }.flatMapLatest { (filters, searchQuery) ->
        issuesRepository.getIssuesPaging(filtersData = filters, query = searchQuery)
    }.cachedIn(viewModelScope)

    init {
        loadFiltersData()
        session.issuesFilters
            .onEach { filters ->
                _state.update {
                    it.copy(activeFilters = filters)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun setSearchQuery(newQuery: String) {
        _searchQuery.update {
            newQuery
        }
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

            resultOf { filtersRepository.getFiltersData(CommonTaskType.Issue) }
                .onSuccess { result ->
                    session.changeIssuesFilters(
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

    private fun selectFilters(filters: FiltersData) {
        session.changeIssuesFilters(filters)
    }
}
