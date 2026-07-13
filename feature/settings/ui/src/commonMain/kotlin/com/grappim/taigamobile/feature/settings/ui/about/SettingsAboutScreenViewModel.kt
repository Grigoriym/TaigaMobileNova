package com.grappim.taigamobile.feature.settings.ui.about

import androidx.lifecycle.ViewModel
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.core.crashapi.CrashReporter
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.privacy_policy_url
import com.grappim.taigamobile.strings.generated.resources.privacy_policy_url_gplay
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SettingsAboutScreenViewModel(appInfoProvider: AppInfoProvider, crashReporter: CrashReporter) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsAboutScreenState(
            privacyPolicyLink = if (crashReporter.isAvailable) {
                NativeText.Resource(RString.privacy_policy_url_gplay)
            } else {
                NativeText.Resource(RString.privacy_policy_url)
            },
            appInfo = appInfoProvider.getAppInfo()
        )
    )
    val state = _state.asStateFlow()
}
