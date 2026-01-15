package com.grappim.taigamobile.feature.login.ui

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.api.ApiConstants
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.login.domain.model.AuthData
import com.grappim.taigamobile.feature.login.domain.model.AuthType
import com.grappim.taigamobile.feature.login.domain.repo.AuthRepository
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val authRepository: AuthRepository, serverStorage: ServerStorage) :
    ViewModel() {

    companion object {
        private const val SERVER_REGEX = """(http|https)://([\w\d-]+\.)+[\w\d-]+(:\d+)?(/\w+)*/?"""
    }

    private val _loginSuccessful = MutableSharedFlow<Boolean>()
    val loginSuccessful = _loginSuccessful.asSharedFlow()

    private val _state = MutableStateFlow(
        LoginState(
            server = TextFieldValue(serverStorage.server),
            onServerValueChange = ::setServer,
            onLoginValueChange = ::setLogin,
            onPasswordValueChange = ::setPassword,
            setIsAlertVisible = ::setIsAlertVisible,
            onActionDialogConfirm = ::login,
            validateAuthData = ::validateAuthData,
            onAuthTypeChange = ::onAuthTypeChange,
            setIsPasswordVisible = ::changePasswordVisibility
        )
    )
    val state = _state.asStateFlow()

    private fun login(authData: AuthData) {
        viewModelScope.launch {
            isLoading(true)
            _state.update {
                it.copy(error = NativeText.Empty)
            }
            authRepository.auth(authData)
                .onSuccess {
                    isLoading(false)
                    _loginSuccessful.emit(true)
                }.onFailure { error ->
                    Timber.d(error)
                    isLoading(false)
                    _state.update {
                        it.copy(error = getErrorMessage(error))
                    }
                }
        }
    }

    private fun login() {
        setIsAlertVisible(false)
        val taigaServer = _state.value.server.text.trim()
        val authType = _state.value.authType
        val password = _state.value.password.text.trim()
        val username = _state.value.login.text.trim()

        login(AuthData(taigaServer, authType, password, username))
    }

    private fun validateAuthData(authType: AuthType) {
        onAuthTypeChange(authType)
        val isServerInputError = !_state.value.server.text.matches(Regex(SERVER_REGEX))
        val isLoginInputError = _state.value.login.text.isBlank()
        val isPasswordInputError = _state.value.password.text.isBlank()

        _state.update {
            it.copy(
                isServerInputError = isServerInputError,
                isLoginInputError = isLoginInputError,
                isPasswordInputError = isPasswordInputError
            )
        }

        if (!(isServerInputError || isLoginInputError || isPasswordInputError)) {
            if (_state.value.server.text.startsWith(ApiConstants.HTTP_SCHEME)) {
                setIsAlertVisible(true)
            } else {
                login()
            }
        }
    }

    private fun isLoading(isLoading: Boolean) {
        _state.update {
            it.copy(
                isLoading = isLoading
            )
        }
    }

    private fun changePasswordVisibility(isVisible: Boolean) {
        _state.update {
            it.copy(
                isPasswordVisible = isVisible
            )
        }
    }

    private fun onAuthTypeChange(authType: AuthType) {
        _state.update {
            it.copy(
                authType = authType
            )
        }
    }

    private fun setIsAlertVisible(newValue: Boolean) {
        _state.update {
            it.copy(
                isAlertVisible = newValue
            )
        }
    }

    private fun setPassword(newValue: TextFieldValue) {
        _state.update {
            it.copy(
                password = newValue,
                isPasswordInputError = false
            )
        }
    }

    private fun setLogin(newValue: TextFieldValue) {
        _state.update {
            it.copy(
                login = newValue,
                isLoginInputError = false
            )
        }
    }

    private fun setServer(newValue: TextFieldValue) {
        _state.update {
            it.copy(
                server = newValue,
                isServerInputError = false
            )
        }
    }
}
