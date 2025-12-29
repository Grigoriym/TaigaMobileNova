package com.grappim.taigamobile.feature.scrum.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.ui.delegates.sprint.WorkItemSprintDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.sprint.WorkItemSprintDelegateImpl
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ScrumViewModel @Inject constructor(
    private val sprintsRepository: SprintsRepository,
    private val session: Session,
    private val userStoriesRepository: UserStoriesRepository,
    private val filtersRepository: FiltersRepository,
    private val dateTimeUtils: DateTimeUtils
) : ViewModel(),
    WorkItemSprintDelegate by WorkItemSprintDelegateImpl(
        dateTimeUtils = dateTimeUtils,
        sprintsRepository = sprintsRepository
    ) {

    private val _state = MutableStateFlow(
        ScrumState(
            onSelectFilters = ::selectFilters,
            retryLoadFilters = ::retryLoadFilters,
            onCreateSprintConfirm = ::createSprint,
            onSetSearchQuery = ::setSearchQuery,
            onCreateSprintClick = ::onCreateSprintClick
        )
    )
    val state = _state.asStateFlow()

    private val _reloadOpenSprints = Channel<Unit>()
    val reloadOpenSprints = _reloadOpenSprints.receiveAsFlow()
    val openSprints = sprintsRepository.getSprints(isClosed = false)
        .cachedIn(viewModelScope)

    val closedSprints = sprintsRepository.getSprints(isClosed = true)
        .cachedIn(viewModelScope)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val userStories: Flow<PagingData<WorkItem>> = combine(
        session.scrumFilters,
        searchQuery
    ) { filters, query ->
        userStoriesRepository.getUserStoriesPaging(filters, query)
    }.flatMapLatest {
        it
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

    private fun onCreateSprintClick() {
        setInitialSprint()
        setSprintDialogVisibility(true)
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

    private fun createSprint() {
        viewModelScope.launch {
            createSprint(
                doOnPreExecute = {
                    _state.update {
                        it.copy(
                            isLoading = true
                        )
                    }
                },
                doOnSuccess = {
                    _state.update {
                        it.copy(
                            isLoading = false
                        )
                    }
                    _reloadOpenSprints.send(Unit)
                },
                doOnError = {
                    _state.update {
                        it.copy(
                            isLoading = false
                        )
                    }
                }
            )
        }
    }
}
