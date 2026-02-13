package com.grappim.taigamobile.feature.settings.ui.user

import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.utils.ui.NativeText

data class SettingsUserScreenState(
    val user: User? = null,
    val serverUrl: String = "",
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty
)
