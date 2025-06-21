package com.grappim.taigamobile.feature.login.ui

import androidx.compose.ui.text.input.TextFieldValue
import com.grappim.taigamobile.feature.login.domain.model.AuthType

data class LoginState(
    val server: TextFieldValue,
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
    val validateAuthData: (authType: AuthType) -> Unit,

    val authType: AuthType = AuthType.NORMAL,
    val onAuthTypeChange: (AuthType) -> Unit,

    val isLoading: Boolean = false,

    val isPasswordVisible: Boolean = false,
    val setIsPasswordVisible: (Boolean) -> Unit
)
