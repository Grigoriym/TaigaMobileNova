package com.grappim.taigamobile.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.Settings
import com.grappim.taigamobile.core.storage.ThemeSetting
import com.grappim.taigamobile.domain.repositories.IUsersRepository
import com.grappim.taigamobile.ui.utils.loadOrError
import com.grappim.taigamobile.ui.utils.mutableResultFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val session: Session,
    private val settings: Settings,
    private val userRepository: IUsersRepository,
    appInfoProvider: com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
) : ViewModel() {

    private val _state: MutableStateFlow<SettingsState> = MutableStateFlow(
        SettingsState(
            appInfo = appInfoProvider.getAppInfo(),
            serverUrl = session.server
        )
    )
    val state = _state.asStateFlow()

    val user = mutableResultFlow<User>()

    val themeSetting by lazy { settings.themeSetting }

    fun onOpen() = viewModelScope.launch {
        user.loadOrError(preserveValue = false) { userRepository.getMe() }
    }

    fun logout() {
        session.reset()
    }

    fun switchTheme(theme: ThemeSetting) {
        settings.changeThemeSetting(theme)
    }
}
