package com.grappim.taigamobile.testing.storage

import com.grappim.taigamobile.core.storage.auth.AuthStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeAuthStorage(
    var tokenToReturn: String = "",
    var refreshTokenToReturn: String = "",
) : AuthStorage {

    var clearCalled = false
    var setCredentialsToken: String? = null
    var setCredentialsRefreshToken: String? = null

    override val isLoggedIn: Flow<Boolean> = flowOf(false)

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
