package com.grappim.taigamobile.feature.settings.ui.about

import androidx.lifecycle.ViewModel
import com.grappim.kit.appinfo.AppInfoProvider
import com.grappim.kit.crash.CrashReporter
import com.grappim.kit.uikit.NativeText
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.privacy_policy_url
import com.grappim.taigamobile.strings.generated.resources.privacy_policy_url_gplay
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
            appInfo = buildAppInfo(appInfoProvider)
        )
    )
    val state = _state.asStateFlow()
}

private fun buildAppInfo(appInfoProvider: AppInfoProvider): String {
    val versionName = appInfoProvider.versionName()
    val versionCode = appInfoProvider.versionCode()
    val buildType = appInfoProvider.buildType()
    return "$versionName - $versionCode - $buildType"
}
