package com.grappim.taigamobile.feature.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.api.ApiConstants
import com.grappim.taigamobile.core.logger.logcat
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.login.domain.launcher.GithubOAuthLauncher
import com.grappim.taigamobile.feature.login.domain.model.AuthData
import com.grappim.taigamobile.feature.login.domain.model.AuthType
import com.grappim.taigamobile.feature.login.domain.repo.AuthRepository
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class LoginViewModel(
    private val authRepository: AuthRepository,
    serverStorage: ServerStorage,
    private val githubOAuthLauncher: GithubOAuthLauncher
) : ViewModel() {

    companion object {
        private const val SERVER_REGEX = """(http|https)://([\w\d-]+\.)+[\w\d-]+(:\d+)?(/\w+)*/?"""
        private const val GITHUB_OAUTH_URL =
            "https://github.com/login/oauth/authorize?client_id=%CLIENT_ID%&state=github&scope=user:email"
    }

    private val _loginSuccessful = MutableSharedFlow<Boolean>()
    val loginSuccessful = _loginSuccessful.asSharedFlow()

    private val _state = MutableStateFlow(
        LoginState(
            server = serverStorage.server,
            onServerValueChange = ::setServer,
            onLoginValueChange = ::setLogin,
            onPasswordValueChange = ::setPassword,
            setIsAlertVisible = ::setIsAlertVisible,
            onActionDialogConfirm = ::onActionDialogConfirm,
            validateAuthData = ::validateAuthData,
            onAuthTypeChange = ::onAuthTypeChange,
            setIsPasswordVisible = ::changePasswordVisibility,
            onGithubLoginClick = ::validateGithubAuth
        )
    )
    val state = _state.asStateFlow()

    private fun onActionDialogConfirm() {
        when (_state.value.authType) {
            AuthType.GITHUB -> {
                setIsAlertVisible(false)
                startGithubOAuth()
            }

            else -> login()
        }
    }

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
                    logcat(throwable = error) {
                        "Login error"
                    }
                    isLoading(false)
                    _state.update {
                        it.copy(error = getErrorMessage(error))
                    }
                }
        }
    }

    private fun login() {
        setIsAlertVisible(false)
        val taigaServer = _state.value.server.trim()
        val authType = _state.value.authType
        val password = _state.value.password.trim()
        val username = _state.value.login.trim()

        login(AuthData(taigaServer, authType, password, username))
    }

    private fun validateAuthData(authType: AuthType) {
        onAuthTypeChange(authType)
        val isServerInputError = !_state.value.server.matches(Regex(SERVER_REGEX))
        val isLoginInputError = _state.value.login.isBlank()
        val isPasswordInputError = _state.value.password.isBlank()

        _state.update {
            it.copy(
                isServerInputError = isServerInputError,
                isLoginInputError = isLoginInputError,
                isPasswordInputError = isPasswordInputError
            )
        }

        if (!(isServerInputError || isLoginInputError || isPasswordInputError)) {
            if (_state.value.server.startsWith(ApiConstants.HTTP_SCHEME)) {
                setIsAlertVisible(true)
            } else {
                login()
            }
        }
    }

    private fun validateGithubAuth() {
        onAuthTypeChange(AuthType.GITHUB)
        val isServerInputError = !_state.value.server.matches(Regex(SERVER_REGEX))
        _state.update { it.copy(isServerInputError = isServerInputError) }
        if (!isServerInputError) {
            if (_state.value.server.startsWith(ApiConstants.HTTP_SCHEME)) {
                setIsAlertVisible(true)
            } else {
                startGithubOAuth()
            }
        }
    }

    private fun startGithubOAuth() {
        viewModelScope.launch {
            isLoading(true)
            _state.update { it.copy(error = NativeText.Empty) }
            authRepository.getGithubClientId(_state.value.server.trim())
                .onSuccess { clientId ->
                    isLoading(false)
                    val url = GITHUB_OAUTH_URL.replace("%CLIENT_ID%", clientId)
                    runCatching { githubOAuthLauncher.launch(url) }
                        .onSuccess { code -> authWithGithub(code) }
                        .onFailure { error ->
                            logcat(throwable = error) { "GitHub OAuth launcher error" }
                            _state.update { it.copy(error = getErrorMessage(error)) }
                        }
                }
                .onFailure { error ->
                    logcat(throwable = error) { "GitHub OAuth error" }
                    isLoading(false)
                    _state.update { it.copy(error = getErrorMessage(error)) }
                }
        }
    }

    private fun authWithGithub(code: String) {
        viewModelScope.launch {
            isLoading(true)
            _state.update { it.copy(error = NativeText.Empty) }
            authRepository.authWithGithub(code)
                .onSuccess {
                    isLoading(false)
                    _loginSuccessful.emit(true)
                }
                .onFailure { error ->
                    logcat(throwable = error) { "GitHub auth error" }
                    isLoading(false)
                    _state.update { it.copy(error = getErrorMessage(error)) }
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

    private fun setPassword(newValue: String) {
        _state.update {
            it.copy(
                password = newValue,
                isPasswordInputError = false
            )
        }
    }

    private fun setLogin(newValue: String) {
        _state.update {
            it.copy(
                login = newValue,
                isLoginInputError = false
            )
        }
    }

    private fun setServer(newValue: String) {
        _state.update {
            it.copy(
                server = newValue,
                isServerInputError = false
            )
        }
    }
}
