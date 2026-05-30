package com.grappim.taigamobile.feature.login.domain.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

interface GithubAuthCallbackHandler {
    val code: StateFlow<String?>
    fun onCodeReceived(code: String)
    fun clear()
}

@Single
class GithubAuthCallbackHandlerImpl : GithubAuthCallbackHandler {
    private val _code = MutableStateFlow<String?>(null)
    override val code = _code.asStateFlow()

    override fun onCodeReceived(code: String) {
        _code.value = code
    }

    override fun clear() {
        _code.value = null
    }
}
