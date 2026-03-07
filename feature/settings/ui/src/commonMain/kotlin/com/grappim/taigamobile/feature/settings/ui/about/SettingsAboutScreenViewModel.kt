package com.grappim.taigamobile.feature.settings.ui.about

import androidx.lifecycle.ViewModel
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SettingsAboutScreenViewModel(appInfoProvider: AppInfoProvider) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsAboutScreenState(
            appInfo = appInfoProvider.getAppInfo()
        )
    )
    val state = _state.asStateFlow()
}
