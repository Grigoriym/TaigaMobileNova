package com.grappim.taigamobile.feature.login.domain.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object GithubAuthCallbackHandler {
    private val _code = MutableStateFlow<String?>(null)
    val code = _code.asStateFlow()

    fun onCodeReceived(code: String) {
        _code.value = code
    }

    fun clear() {
        _code.value = null
    }
}
