package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.login.domain.model.GithubAuthCallbackHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeGithubAuthCallbackHandler : GithubAuthCallbackHandler {
    private val _code = MutableStateFlow<String?>(null)
    override val code = _code.asStateFlow()

    override fun onCodeReceived(code: String) {
        _code.value = code
    }

    override fun clear() {
        _code.value = null
    }
}