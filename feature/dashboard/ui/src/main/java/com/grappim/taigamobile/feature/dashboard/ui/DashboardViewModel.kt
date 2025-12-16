package com.grappim.taigamobile.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.dashboard.domain.GetDashboardDataUseCase
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val session: Session
) : ViewModel() {

    private val _state = MutableStateFlow(
        DashboardState(
            onLoad = ::load
        )
    )
    val state = _state.asStateFlow()

    private fun load() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = NativeText.Empty
                )
            }
            getDashboardDataUseCase.getData(
                userId = session.userId,
                projectId = taigaStorage.currentProjectIdFlow.first()
            ).onSuccess { result ->
                _state.update {
                    it.copy(
                        currentProjectId = taigaStorage.currentProjectIdFlow.first(),
                        workingOn = result.workingOn,
                        watching = result.watching,
                        isLoading = false
                    )
                }
            }.onFailure { error ->
                Timber.e(error)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = getErrorMessage(error)
                    )
                }
            }
        }
    }
}
