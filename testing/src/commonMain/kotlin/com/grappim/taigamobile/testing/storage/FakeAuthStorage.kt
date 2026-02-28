package com.grappim.taigamobile.testing.storage

import com.grappim.taigamobile.core.storage.auth.AuthStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAuthStorage(
    initialLoggedIn: Boolean = false,
    var tokenToReturn: String = "",
    var refreshTokenToReturn: String = "",
) : AuthStorage {

    var clearCalled = false
    var setCredentialsToken: String? = null
    var setCredentialsRefreshToken: String? = null

    private val _isLoggedIn = MutableStateFlow(initialLoggedIn)
    override val isLoggedIn: Flow<Boolean> = _isLoggedIn.asStateFlow()

    fun setLoggedIn(value: Boolean) { _isLoggedIn.value = value }

    override suspend fun getToken(): String = tokenToReturn
    override suspend fun getRefreshToken(): String = refreshTokenToReturn

    override suspend fun setAuthCredentials(token: String, refreshToken: String) {
        setCredentialsToken = token
        setCredentialsRefreshToken = refreshToken
    }

    override suspend fun clear() {
        clearCalled = true
    }
}
