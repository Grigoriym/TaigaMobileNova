package com.grappim.taigamobile.feature.settings.ui.about

import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.github_issues_url
import com.grappim.taigamobile.strings.generated.resources.github_url
import com.grappim.taigamobile.strings.generated.resources.privacy_policy_url
import com.grappim.taigamobile.utils.ui.NativeText

data class SettingsAboutScreenState(
    val githubRepoLink: NativeText = NativeText.Resource(RString.github_url),
    val githubIssuesLink: NativeText = NativeText.Resource(RString.github_issues_url),
    val privacyPolicyLink: NativeText = NativeText.Resource(RString.privacy_policy_url),
    val appInfo: String = ""
)
