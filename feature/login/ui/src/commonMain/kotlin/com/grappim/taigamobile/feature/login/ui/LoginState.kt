package com.grappim.taigamobile.feature.login.ui

import com.grappim.taigamobile.feature.login.domain.model.AuthType
import com.grappim.taigamobile.utils.ui.NativeText

data class LoginState(
    val server: String,
    val onServerValueChange: (String) -> Unit,
    val isServerInputError: Boolean = false,

    val login: String = "",
    val onLoginValueChange: (String) -> Unit,
    val isLoginInputError: Boolean = false,

    val error: NativeText = NativeText.Empty,

    val password: String = "",
    val onPasswordValueChange: (String) -> Unit,
    val isPasswordInputError: Boolean = false,

    val isAlertVisible: Boolean = false,
    val setIsAlertVisible: (Boolean) -> Unit,

    val onActionDialogConfirm: () -> Unit,
    val validateAuthData: (authType: AuthType) -> Unit,

    val authType: AuthType = AuthType.NORMAL,
    val onAuthTypeChange: (AuthType) -> Unit,

    val isLoading: Boolean = false,

    val isPasswordVisible: Boolean = false,
    val setIsPasswordVisible: (Boolean) -> Unit
)
