package com.grappim.taigamobile.login.ui

import androidx.compose.ui.text.input.TextFieldValue
import com.grappim.taigamobile.data.api.NetworkConstants
import com.grappim.taigamobile.domain.entities.AuthType
import com.grappim.taigamobile.login.domain.AuthData

data class LoginState(
    val server: TextFieldValue = TextFieldValue(NetworkConstants.DEFAULT_HOST),
    val onServerValueChange: (TextFieldValue) -> Unit,
    val isServerInputError: Boolean = false,

    val login: TextFieldValue = TextFieldValue(),
    val onLoginValueChange: (TextFieldValue) -> Unit,
    val isLoginInputError: Boolean = false,

    val password: TextFieldValue = TextFieldValue(),
    val onPasswordValueChange: (TextFieldValue) -> Unit,
    val isPasswordInputError: Boolean = false,

    val isAlertVisible: Boolean = false,
    val setIsAlertVisible: (Boolean) -> Unit,

    val onActionDialogConfirm: () -> Unit,
    val onLoginContinue: (authType: AuthType) -> Unit,

    val authType: AuthType = AuthType.NORMAL,
    val onAuthTypeChange: (AuthType) -> Unit,

    val onLogin: (authData: AuthData) -> Unit,

    val isLoading: Boolean = false
)
