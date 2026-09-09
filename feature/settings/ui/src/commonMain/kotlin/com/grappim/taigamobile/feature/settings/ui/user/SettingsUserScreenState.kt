package com.grappim.taigamobile.feature.settings.ui.user

import com.grappim.kit.uikit.NativeText
import com.grappim.taigamobile.feature.users.domain.User

data class SettingsUserScreenState(
    val user: User? = null,
    val serverUrl: String = "",
    val isUnencryptedConnection: Boolean = false,
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty
)
