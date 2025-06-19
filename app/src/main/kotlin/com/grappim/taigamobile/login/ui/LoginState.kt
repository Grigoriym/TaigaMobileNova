package com.grappim.taigamobile.login.ui

import androidx.compose.ui.text.input.TextFieldValue
import com.grappim.taigamobile.core.api.ApiConstants
import com.grappim.taigamobile.feature.login.domain.model.AuthType
import com.grappim.taigamobile.feature.login.domain.model.AuthData

data class LoginState(
    val server: TextFieldValue = TextFieldValue(ApiConstants.DEFAULT_HOST),
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
