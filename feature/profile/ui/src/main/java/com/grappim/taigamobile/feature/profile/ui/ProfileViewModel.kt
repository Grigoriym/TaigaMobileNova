package com.grappim.taigamobile.feature.profile.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.profile.domain.GetProfileDataUseCase
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileDataUseCase: GetProfileDataUseCase,
    private val taigaStorage: TaigaStorage,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val navRoute = savedStateHandle.toRoute<ProfileNavDestination>()
    private val userId: Long
        get() = navRoute.userId

    private val _state = MutableStateFlow(
        ProfileState(
            onReload = ::loadProfile
        )
    )
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    error = NativeText.Empty,
                    isLoading = false
                )
            }
            getProfileDataUseCase.getProfileData(userId)
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            user = result.user,
                            userStats = result.userStats,
                            projects = result.projects,
                            currentProjectId = taigaStorage.currentProjectIdFlow.first()
                        )
                    }
                }.onFailure { error ->
                    Timber.e(error)
                    _state.update {
                        it.copy(
                            error = getErrorMessage(error),
                            isLoading = false
                        )
                    }
                }
        }
    }
}
