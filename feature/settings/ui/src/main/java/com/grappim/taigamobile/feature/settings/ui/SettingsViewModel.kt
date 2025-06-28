package com.grappim.taigamobile.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.core.storage.ThemeSettings
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.SnackbarStateViewModel
import com.grappim.taigamobile.utils.ui.SnackbarStateViewModelImpl
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val taigaStorage: TaigaStorage,
    serverStorage: ServerStorage,
    appInfoProvider: AppInfoProvider
) : ViewModel(),
    SnackbarStateViewModel by SnackbarStateViewModelImpl() {

    private val _state: MutableStateFlow<SettingsState> = MutableStateFlow(
        SettingsState(
            appInfo = appInfoProvider.getAppInfo(),
            serverUrl = serverStorage.server,
            onThemeChanged = ::switchTheme,
            showSnackbar = ::showSnackbar,
            getThemeTitle = ::getThemeTitle
        )
    )
    val state = _state.asStateFlow()

    init {
        _state.update { it.copy(isLoading = true) }
        taigaStorage.themeSettings.onEach { settings ->
            val title = getThemeTitle(settings)
            _state.update {
                it.copy(
                    themeSettings = settings,
                    themeDropDownTitle = title
                )
            }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            usersRepository.getMeResult()
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            user = result,
                            isLoading = false
                        )
                    }
                }.onFailure { e ->
                    val errorMessage = getErrorMessage(e)
                    showSnackbar(errorMessage)
                    _state.update {
                        it.copy(
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun getThemeTitle(theme: ThemeSettings): NativeText = NativeText.Resource(
        when (theme) {
            ThemeSettings.System -> RString.theme_system
            ThemeSettings.Light -> RString.theme_light
            ThemeSettings.Dark -> RString.theme_dark
        }
    )

    private fun showSnackbar(msg: NativeText) {
        viewModelScope.launch {
            showSnackbarSuspend(msg)
        }
    }

    private fun switchTheme(theme: ThemeSettings) {
        viewModelScope.launch {
            taigaStorage.setThemSetting(theme)
        }
    }
}
