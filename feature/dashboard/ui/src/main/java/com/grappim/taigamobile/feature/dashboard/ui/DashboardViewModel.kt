package com.grappim.taigamobile.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.dashboard.domain.GetMyWorkItemsUseCase
import com.grappim.taigamobile.feature.dashboard.domain.GetRecentActivityUseCase
import com.grappim.taigamobile.feature.dashboard.domain.GetRecentlyCompletedItemsUseCase
import com.grappim.taigamobile.feature.dashboard.domain.GetWatchingItemsUseCase
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val taigaStorage: TaigaStorage,
    private val session: Session,
    private val getWatchingItemsUseCase: GetWatchingItemsUseCase,
    private val getMyWorkItemsUseCase: GetMyWorkItemsUseCase,
    private val getRecentActivityUseCase: GetRecentActivityUseCase,
    private val getRecentlyCompletedItemsUseCase: GetRecentlyCompletedItemsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(
        DashboardState(
            onToggleWatching = ::toggleWatching,
            onToggleMyWork = ::toggleMyWork,
            onToggleRecentActivity = ::toggleRecentActivity,
            onToggleRecentlyCompleted = ::toggleRecentlyCompleted,
            onRetryWatching = ::loadWatching,
            onRetryMyWork = ::loadMyWork,
            onRetryRecentActivity = ::loadRecentActivity,
            onRetryRecentlyCompleted = ::loadRecentlyCompleted
        )
    )
    val state = _state.asStateFlow()

    init {
        loadAll()
    }

    private fun loadAll() {
        loadWatching()
        loadMyWork()
        loadRecentActivity()
        loadRecentlyCompleted()
    }

    private fun toggleWatching() {
        _state.update {
            it.copy(watchingSection = it.watchingSection.copy(isExpanded = !it.watchingSection.isExpanded))
        }
    }

    private fun toggleMyWork() {
        _state.update {
            it.copy(myWorkSection = it.myWorkSection.copy(isExpanded = !it.myWorkSection.isExpanded))
        }
    }

    private fun toggleRecentActivity() {
        _state.update {
            it.copy(
                recentActivitySection = it.recentActivitySection.copy(isExpanded = !it.recentActivitySection.isExpanded)
            )
        }
    }

    private fun toggleRecentlyCompleted() {
        _state.update {
            it.copy(
                recentlyCompletedSection = it.recentlyCompletedSection.copy(
                    isExpanded = !it.recentlyCompletedSection.isExpanded
                )
            )
        }
    }

    private fun loadWatching() {
        viewModelScope.launch {
            val isRetry = _state.value.watchingSection.error.isNotEmpty()
            _state.update {
                it.copy(
                    watchingSection = it.watchingSection.copy(
                        isLoading = !isRetry,
                        isRetrying = isRetry,
                        error = if (isRetry) it.watchingSection.error else NativeText.Empty
                    )
                )
            }

            val userId = session.userId
            val projectId = taigaStorage.currentProjectIdFlow.first()

            getWatchingItemsUseCase.getData(userId, projectId)
                .onSuccess { items ->
                    _state.update {
                        it.copy(
                            currentProjectId = projectId,
                            watchingSection = it.watchingSection.copy(
                                items = items.toImmutableList(),
                                isLoading = false,
                                isRetrying = false,
                                error = NativeText.Empty
                            )
                        )
                    }
                }
                .onFailure { error ->
                    Timber.e(error)
                    _state.update {
                        it.copy(
                            watchingSection = it.watchingSection.copy(
                                isLoading = false,
                                isRetrying = false,
                                error = getErrorMessage(error)
                            )
                        )
                    }
                }
        }
    }

    private fun loadMyWork() {
        viewModelScope.launch {
            val isRetry = _state.value.myWorkSection.error.isNotEmpty()
            _state.update {
                it.copy(
                    myWorkSection = it.myWorkSection.copy(
                        isLoading = !isRetry,
                        isRetrying = isRetry,
                        error = if (isRetry) it.myWorkSection.error else NativeText.Empty
                    )
                )
            }

            val userId = session.userId
            val projectId = taigaStorage.currentProjectIdFlow.first()

            getMyWorkItemsUseCase.getData(userId, projectId)
                .onSuccess { items ->
                    _state.update {
                        it.copy(
                            currentProjectId = projectId,
                            myWorkSection = it.myWorkSection.copy(
                                items = items.toImmutableList(),
                                isLoading = false,
                                isRetrying = false,
                                error = NativeText.Empty
                            )
                        )
                    }
                }
                .onFailure { error ->
                    Timber.e(error)
                    _state.update {
                        it.copy(
                            myWorkSection = it.myWorkSection.copy(
                                isLoading = false,
                                isRetrying = false,
                                error = getErrorMessage(error)
                            )
                        )
                    }
                }
        }
    }

    private fun loadRecentActivity() {
        viewModelScope.launch {
            val isRetry = _state.value.recentActivitySection.error.isNotEmpty()
            _state.update {
                it.copy(
                    recentActivitySection = it.recentActivitySection.copy(
                        isLoading = !isRetry,
                        isRetrying = isRetry,
                        error = if (isRetry) it.recentActivitySection.error else NativeText.Empty
                    )
                )
            }

            val projectId = taigaStorage.currentProjectIdFlow.first()

            getRecentActivityUseCase.getData(projectId)
                .onSuccess { items ->
                    _state.update {
                        it.copy(
                            currentProjectId = projectId,
                            recentActivitySection = it.recentActivitySection.copy(
                                items = items.toImmutableList(),
                                isLoading = false,
                                isRetrying = false,
                                error = NativeText.Empty
                            )
                        )
                    }
                }
                .onFailure { error ->
                    Timber.e(error)
                    _state.update {
                        it.copy(
                            recentActivitySection = it.recentActivitySection.copy(
                                isLoading = false,
                                isRetrying = false,
                                error = getErrorMessage(error)
                            )
                        )
                    }
                }
        }
    }

    private fun loadRecentlyCompleted() {
        viewModelScope.launch {
            val isRetry = _state.value.recentlyCompletedSection.error.isNotEmpty()
            _state.update {
                it.copy(
                    recentlyCompletedSection = it.recentlyCompletedSection.copy(
                        isLoading = !isRetry,
                        isRetrying = isRetry,
                        error = if (isRetry) it.recentlyCompletedSection.error else NativeText.Empty
                    )
                )
            }

            val projectId = taigaStorage.currentProjectIdFlow.first()

            getRecentlyCompletedItemsUseCase.getData(projectId)
                .onSuccess { items ->
                    _state.update {
                        it.copy(
                            currentProjectId = projectId,
                            recentlyCompletedSection = it.recentlyCompletedSection.copy(
                                items = items.toImmutableList(),
                                isLoading = false,
                                isRetrying = false,
                                error = NativeText.Empty
                            )
                        )
                    }
                }
                .onFailure { error ->
                    Timber.e(error)
                    _state.update {
                        it.copy(
                            recentlyCompletedSection = it.recentlyCompletedSection.copy(
                                isLoading = false,
                                isRetrying = false,
                                error = getErrorMessage(error)
                            )
                        )
                    }
                }
        }
    }
}
