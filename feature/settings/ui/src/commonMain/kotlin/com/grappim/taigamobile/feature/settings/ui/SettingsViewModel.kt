package com.grappim.taigamobile.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.logger.LogPriority
import com.grappim.taigamobile.core.logger.logcat
import com.grappim.taigamobile.core.storage.auth.AuthStateManager
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SettingsViewModel(
    private val projectsRepository: ProjectsRepository,
    private val authStateManager: AuthStateManager
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsState(
            setIsLogoutConfirmationVisible = ::showLogoutConfirmation,
            onLogout = ::logout
        )
    )
    val state = _state.asStateFlow()

    private var loadCurrentProjectJob: Job? = null

    init {
        loadCurrentProjectJob = viewModelScope.launch {
            resultOf {
                projectsRepository.getCurrentProjectSimple()
            }.onSuccess { project ->
                _state.update {
                    it.copy(canSeeAttributes = project.isAdmin)
                }
            }.onFailure { error ->
                logcat(LogPriority.ERROR, throwable = error) { "failed to load current project" }
            }
        }
    }

    private fun logout() {
        // Settings loads the current project on init; if that load is still in flight when
        // logout is confirmed, it can race authStateManager.logoutSuspend()'s table clear and
        // crash on the now-missing row. Cancel it first — its result no longer matters.
        loadCurrentProjectJob?.cancel()

        viewModelScope.launch {
            _state.update {
                it.copy(isLogoutConfirmationVisible = false)
            }

            authStateManager.logoutSuspend()
        }
    }

    private fun showLogoutConfirmation(isVisible: Boolean) {
        _state.update { it.copy(isLogoutConfirmationVisible = isVisible) }
    }
}
