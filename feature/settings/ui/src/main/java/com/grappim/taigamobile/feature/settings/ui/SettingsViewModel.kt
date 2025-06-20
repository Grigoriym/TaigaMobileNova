package com.grappim.taigamobile.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.Settings
import com.grappim.taigamobile.core.storage.ThemeSetting
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.utils.ui.loadOrError
import com.grappim.taigamobile.utils.ui.mutableResultFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val session: Session,
    private val settings: Settings,
    private val userRepository: UsersRepository,
    appInfoProvider: AppInfoProvider
) : ViewModel() {

    private val _state: MutableStateFlow<SettingsState> = MutableStateFlow(
        SettingsState(
            appInfo = appInfoProvider.getAppInfo(),
            serverUrl = session.server,
            setIsAlertVisible = ::setAlertVisible
        )
    )
    val state = _state.asStateFlow()

    val user = mutableResultFlow<User>()

    init {
        onOpen()
    }

    val themeSetting by lazy { settings.themeSetting }

    private fun setAlertVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isAlertVisible = isVisible)
        }
    }

    fun onOpen() {
        viewModelScope.launch {
            user.loadOrError(preserveValue = false) { userRepository.getMe() }
        }
    }

    fun logout() {
        session.reset()
    }

    fun switchTheme(theme: ThemeSetting) {
        settings.changeThemeSetting(theme)
    }
}
