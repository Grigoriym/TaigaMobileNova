package com.grappim.taigamobile.feature.settings.ui.about

import androidx.lifecycle.ViewModel
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsAboutScreenViewModel @Inject constructor(appInfoProvider: AppInfoProvider) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsAboutScreenState(
            appInfo = appInfoProvider.getAppInfo()
        )
    )
    val state = _state.asStateFlow()
}
