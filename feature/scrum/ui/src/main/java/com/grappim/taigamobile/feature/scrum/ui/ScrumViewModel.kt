package com.grappim.taigamobile.feature.scrum.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.filters.domain.RetryFiltersSignalDelegate
import com.grappim.taigamobile.feature.filters.domain.RetryFiltersSignalDelegateImpl
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.delegates.SnackbarDelegate
import com.grappim.taigamobile.utils.ui.delegates.SnackbarDelegateImpl
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
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
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ScrumViewModel @Inject constructor(
    private val sprintsRepository: SprintsRepository,
    private val session: Session,
    private val userStoriesRepository: UserStoriesRepository,
    private val filtersRepository: FiltersRepository,
    taigaStorage: TaigaStorage
) : ViewModel(),
    SnackbarDelegate by SnackbarDelegateImpl(),
    RetryFiltersSignalDelegate by RetryFiltersSignalDelegateImpl() {

    private val _state = MutableStateFlow(
        ScrumState(
            setIsCreateSprintDialogVisible = ::setIsCreateSprintDialogVisible,
            onSelectFilters = ::selectFilters,
            retryLoadFilters = ::retryLoadFilters,
            onCreateSprint = ::createSprint
        )
    )
    val state = _state.asStateFlow()

    private val retryOpenSprints = MutableSharedFlow<Unit>()

    val openSprints = merge(
        taigaStorage.currentProjectIdFlow.distinctUntilChanged(),
        retryOpenSprints
    ).flatMapLatest {
        sprintsRepository.getSprints(isClosed = false)
    }.cachedIn(viewModelScope)

    val closedSprints = merge(
        taigaStorage.currentProjectIdFlow.distinctUntilChanged()
    ).flatMapLatest {
        sprintsRepository.getSprints(isClosed = true)
    }.cachedIn(viewModelScope)

    val userStories: Flow<PagingData<CommonTask>> = session.scrumFilters.flatMapLatest { filters ->
        userStoriesRepository.getUserStoriesPaging(filters)
    }.cachedIn(viewModelScope)

    val filters = merge(
        taigaStorage.currentProjectIdFlow.distinctUntilChanged(),
        retryFiltersSignal
    ).mapLatest {
        loadFiltersData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FiltersDataDTO()
    )

    init {
        session.scrumFilters.onEach { filters ->
            _state.update {
                it.copy(activeFilters = filters)
            }
        }.launchIn(viewModelScope)
    }

    private fun retryLoadFilters() {
        viewModelScope.launch {
            signalRetryFilters()
        }
    }

    private fun setIsCreateSprintDialogVisible(newValue: Boolean) {
        _state.update {
            it.copy(
                isCreateSprintDialogVisible = newValue
            )
        }
    }

    private suspend fun loadFiltersData(): FiltersDataDTO {
        _state.update {
            it.copy(
                isFiltersLoading = true,
                isFiltersError = false
            )
        }

        val result = filtersRepository.getFiltersDataResultOld(
            commonTaskType = CommonTaskType.UserStory,
            isCommonTaskFromBacklog = true
        )
        return if (result.isSuccess) {
            val filters = result.getOrThrow()

            _state.update {
                it.copy(
                    isFiltersLoading = false,
                    isFiltersError = false
                )
            }

            session.changeScrumFilters(_state.value.activeFilters.updateData(filters))

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

            FiltersDataDTO()
        }
    }

    private fun selectFilters(filters: FiltersDataDTO) {
        session.changeScrumFilters(filters)
    }

    private fun createSprint(name: String, start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            setIsCreateSprintDialogVisible(false)
            _state.update { it.copy(loading = true) }
            sprintsRepository.createSprint(name, start, end).onSuccess {
                _state.update { it.copy(loading = false) }
                retryOpenSprints.emit(Unit)
            }.onFailure { error ->
                _state.update { it.copy(loading = false) }
                Timber.e(error)
                showSnackbarSuspend(getErrorMessage(error))
            }
        }
    }
}
