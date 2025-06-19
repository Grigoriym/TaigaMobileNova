package com.grappim.taigamobile.login.ui

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.feature.login.domain.model.AuthData
import com.grappim.taigamobile.feature.login.domain.model.AuthType
import com.grappim.taigamobile.feature.login.domain.repo.IAuthRepository
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.SnackbarStateViewModel
import com.grappim.taigamobile.utils.ui.SnackbarStateViewModelImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val authRepository: IAuthRepository) :
    ViewModel(),
    SnackbarStateViewModel by SnackbarStateViewModelImpl() {

    companion object {
        private const val SERVER_REGEX = """(http|https)://([\w\d-]+\.)+[\w\d-]+(:\d+)?(/\w+)*/?"""
    }

    private val _loginSuccessful = MutableSharedFlow<Boolean>()
    val loginSuccessful = _loginSuccessful.asSharedFlow()

    private val _loginState = MutableStateFlow(
        LoginState(
            onLogin = ::login,
            onServerValueChange = ::setServer,
            onLoginValueChange = ::setLogin,
            onPasswordValueChange = ::setPassword,
            setIsAlertVisible = ::setIsAlertVisible,
            onActionDialogConfirm = ::loginAction,
            onLoginContinue = ::onLoginContinue,
            onAuthTypeChange = ::onAuthTypeChange
        )
    )
    val loginState = _loginState.asStateFlow()

    fun login(authData: AuthData) {
        viewModelScope.launch {
            isLoading(true)
            authRepository.auth(authData)
                .onSuccess {
                    _loginState.update {
                        it.copy(isLoading = false)
                    }
                    _loginSuccessful.emit(true)
                }.onFailure {
                    isLoading(false)
                    setSnackbarMessageSuspend(NativeText.Resource(RString.login_error_message))
                }
        }
    }

    private fun isLoading(isLoading: Boolean) {
        _loginState.update {
            it.copy(
                isLoading = isLoading
            )
        }
    }

    private fun login() {
        val taigaServer = _loginState.value.server.text.trim()
        val authType = _loginState.value.authType
        val password = _loginState.value.password.text.trim()
        val username = _loginState.value.login.text.trim()

        login(AuthData(taigaServer, authType, password, username))
    }

    private fun onLoginContinue(authType: AuthType) {
        onAuthTypeChange(authType)
        val isServerInputError = !_loginState.value.server.text.matches(Regex(SERVER_REGEX))
        val isLoginInputError = _loginState.value.login.text.isBlank()
        val isPasswordInputError = _loginState.value.password.text.isBlank()

        _loginState.update {
            it.copy(
                isServerInputError = isServerInputError,
                isLoginInputError = isLoginInputError,
                isPasswordInputError = isPasswordInputError
            )
        }

        if (!(isServerInputError || isLoginInputError || isPasswordInputError)) {
            if (_loginState.value.server.text.startsWith("http://")) {
                setIsAlertVisible(true)
            } else {
                loginAction()
            }
        }
    }

    private fun onAuthTypeChange(authType: AuthType) {
        _loginState.update {
            it.copy(
                authType = authType
            )
        }
    }

    private fun loginAction() {
        setIsAlertVisible(false)
        login()
    }

    private fun setIsAlertVisible(newValue: Boolean) {
        _loginState.update {
            it.copy(
                isAlertVisible = newValue
            )
        }
    }

    private fun setPassword(newValue: TextFieldValue) {
        _loginState.update {
            it.copy(
                password = newValue,
                isPasswordInputError = false
            )
        }
    }

    private fun setLogin(newValue: TextFieldValue) {
        _loginState.update {
            it.copy(
                login = newValue,
                isLoginInputError = false
            )
        }
    }

    private fun setServer(newValue: TextFieldValue) {
        _loginState.update {
            it.copy(
                server = newValue,
                isServerInputError = false
            )
        }
    }
}
