package com.grappim.taigamobile.feature.profile.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.logger.logcat
import com.grappim.taigamobile.core.storage.KmpTaigaSessionStorage
import com.grappim.taigamobile.feature.issues.domain.GetProfileDataUseCase
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ProfileViewModel(
    private val getProfileDataUseCase: GetProfileDataUseCase,
    private val taigaSessionStorage: KmpTaigaSessionStorage,
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
                            currentProjectId = taigaSessionStorage.getCurrentProjectId()
                        )
                    }
                }.onFailure { error ->
                    logcat(throwable = error) {
                        "Error loading profile"
                    }
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
