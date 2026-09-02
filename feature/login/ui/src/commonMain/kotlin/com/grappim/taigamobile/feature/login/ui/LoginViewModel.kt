package com.grappim.taigamobile.feature.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.api.ApiConstants
import com.grappim.taigamobile.core.domain.UntrustedCertificateNetworkException
import com.grappim.taigamobile.core.logger.logcat
import com.grappim.taigamobile.core.storage.cert.TrustedCertStorage
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.login.domain.model.AuthData
import com.grappim.taigamobile.feature.login.domain.model.AuthType
import com.grappim.taigamobile.feature.login.domain.repo.AuthRepository
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class LoginViewModel(
    private val authRepository: AuthRepository,
    serverStorage: ServerStorage,
    private val trustedCertStorage: TrustedCertStorage
) : ViewModel() {

    companion object {
        private const val SERVER_REGEX = """(http|https)://([\w\d-]+\.)*[\w\d-]+(:\d+)?(/\w+)*/?"""
        private const val GITHUB_OAUTH_URL =
            "https://github.com/login/oauth/authorize?client_id=%CLIENT_ID%&state=github&scope=user:email"
    }

    private val _loginSuccessful = MutableSharedFlow<Boolean>()
    val loginSuccessful = _loginSuccessful.asSharedFlow()

    private val _openGithubWebView = Channel<String>(Channel.BUFFERED)
    val openGithubWebView = _openGithubWebView.receiveAsFlow()

    // Set together with LoginState.pendingCertTrust whenever a request fails on an untrusted
    // certificate, so onConfirmCertTrust can re-run the exact request that just failed.
    private var pendingRetry: (() -> Unit)? = null

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
            onGithubLoginClick = ::validateGithubAuth,
            onConfirmCertTrust = ::onConfirmCertTrust,
            onDismissCertTrust = ::onDismissCertTrust
        )
    )
    val state = _state.asStateFlow()

    fun onGithubCodeReceived(code: String) {
        authWithGithub(code)
    }

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
            _state.update { it.copy(error = NativeText.Empty) }
            authRepository.auth(authData)
                .onSuccess {
                    isLoading(false)
                    _loginSuccessful.emit(true)
                }.onFailure { error ->
                    handleFailure(error, "Login error") { login(authData) }
                }
        }
    }

    private fun login() {
        setIsAlertVisible(false)
        login(
            AuthData(
                taigaServer = _state.value.server.trim(),
                authType = _state.value.authType,
                password = _state.value.password,
                username = _state.value.login.trim()
            )
        )
    }

    private fun validateAuthData(authType: AuthType) {
        onAuthTypeChange(authType)
        val isServerInputError = !_state.value.server.matches(Regex(SERVER_REGEX))
        val isLoginInputError = _state.value.login.isBlank()
        val isPasswordInputError = _state.value.password.isEmpty()

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
                    _openGithubWebView.send(GITHUB_OAUTH_URL.replace("%CLIENT_ID%", clientId))
                }
                .onFailure { error ->
                    handleFailure(error, "GitHub OAuth error") { startGithubOAuth() }
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
                    handleFailure(error, "GitHub auth error") { authWithGithub(code) }
                }
        }
    }

    private fun handleFailure(error: Throwable, logMessage: String, retry: () -> Unit) {
        logcat(throwable = error) { logMessage }
        isLoading(false)
        if (error is UntrustedCertificateNetworkException) {
            pendingRetry = retry
            _state.update {
                it.copy(pendingCertTrust = error.pendingCertTrust, isCertTrustDialogVisible = true)
            }
        } else {
            _state.update { it.copy(error = getErrorMessage(error)) }
        }
    }

    private fun onConfirmCertTrust() {
        val pendingCertTrust = _state.value.pendingCertTrust ?: return
        val retry = pendingRetry
        pendingRetry = null
        _state.update { it.copy(isCertTrustDialogVisible = false, pendingCertTrust = null) }
        viewModelScope.launch {
            trustedCertStorage.trust(pendingCertTrust)
            retry?.invoke()
        }
    }

    private fun onDismissCertTrust() {
        pendingRetry = null
        _state.update { it.copy(isCertTrustDialogVisible = false, pendingCertTrust = null) }
    }

    private fun isLoading(isLoading: Boolean) {
        _state.update { it.copy(isLoading = isLoading) }
    }

    private fun changePasswordVisibility(isVisible: Boolean) {
        _state.update { it.copy(isPasswordVisible = isVisible) }
    }

    private fun onAuthTypeChange(authType: AuthType) {
        _state.update { it.copy(authType = authType) }
    }

    private fun setIsAlertVisible(newValue: Boolean) {
        _state.update { it.copy(isAlertVisible = newValue) }
    }

    private fun setPassword(newValue: String) {
        _state.update { it.copy(password = newValue, isPasswordInputError = false) }
    }

    private fun setLogin(newValue: String) {
        _state.update { it.copy(login = newValue, isLoginInputError = false) }
    }

    private fun setServer(newValue: String) {
        _state.update { it.copy(server = newValue, isServerInputError = false) }
    }
}
