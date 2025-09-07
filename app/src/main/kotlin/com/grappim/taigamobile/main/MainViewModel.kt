package com.grappim.taigamobile.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.storage.AuthStateManager
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.core.storage.ThemeSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val session: Session,
    taigaStorage: TaigaStorage,
    private val authStateManager: AuthStateManager
) : ViewModel() {

    private val _state = MutableStateFlow(
        MainScreenState(
            setIsLogoutConfirmationVisible = ::showLogoutConfirmation,
            onLogout = ::logout
        )
    )
    val state = _state.asStateFlow()

    val logoutEvent = authStateManager.logoutEvents

    val isLogged by lazy { session.isLogged }

    val theme = taigaStorage.themeSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeSettings.default()
        )

    val isNewUIUsed = taigaStorage.isNewUIUsed
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    private fun logout() {
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
