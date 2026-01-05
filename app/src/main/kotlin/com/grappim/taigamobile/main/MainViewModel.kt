package com.grappim.taigamobile.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.nav.DrawerItem
import com.grappim.taigamobile.core.storage.AuthStateManager
import com.grappim.taigamobile.core.storage.AuthStorage
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.ThemeSettings
import com.grappim.taigamobile.feature.projects.domain.ProjectSimple
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    taigaSessionStorage: TaigaSessionStorage,
    authStorage: AuthStorage,
    private val authStateManager: AuthStateManager,
    projectsRepository: ProjectsRepository,
    private val drawerItemsBuilder: DrawerItemsBuilder
) : ViewModel() {

    private val _state = MutableStateFlow(
        MainScreenState(
            setIsLogoutConfirmationVisible = ::showLogoutConfirmation,
            onLogout = ::logout
        )
    )
    val state = _state.asStateFlow()

    val logoutEvent = authStateManager.logoutEvents

    val isLoggedIn = authStorage.isLoggedIn

    val currentProject: StateFlow<ProjectSimple?> = projectsRepository.getCurrentProjectFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val theme = taigaSessionStorage.themeSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeSettings.default()
        )

    val drawerItems: StateFlow<ImmutableList<DrawerItem>> = currentProject
        .filterNotNull()
        .map { project ->
            drawerItemsBuilder.build(project)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = persistentListOf()
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
