package com.grappim.taigamobile.feature.settings.ui.interfacescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.ThemeSettings
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.utils.ui.NativeText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsInterfaceViewModel @Inject constructor(private val taigaSessionStorage: TaigaSessionStorage) :
    ViewModel() {

    private val _state = MutableStateFlow(
        SettingsInterfaceViewState(
            onThemeChanged = ::switchTheme,
            getThemeTitle = ::getThemeTitle
        )
    )
    val state = _state.asStateFlow()

    init {
        taigaSessionStorage.themeSettings.onEach { settings ->
            val title = getThemeTitle(settings)
            _state.update {
                it.copy(
                    themeSettings = settings,
                    themeDropDownTitle = title
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun getThemeTitle(theme: ThemeSettings): NativeText = NativeText.Resource(
        when (theme) {
            ThemeSettings.System -> RString.theme_system
            ThemeSettings.Light -> RString.theme_light
            ThemeSettings.Dark -> RString.theme_dark
        }
    )

    private fun switchTheme(theme: ThemeSettings) {
        viewModelScope.launch {
            taigaSessionStorage.setThemSetting(theme)
        }
    }
}
