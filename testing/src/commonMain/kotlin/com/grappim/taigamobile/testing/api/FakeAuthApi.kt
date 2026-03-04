package com.grappim.taigamobile.testing.api

import com.grappim.taigamobile.feature.login.data.AuthApi
import com.grappim.taigamobile.feature.login.dto.AuthRequest
import com.grappim.taigamobile.feature.login.dto.AuthResponse
import com.grappim.taigamobile.feature.login.dto.RefreshTokenRequest
import com.grappim.taigamobile.feature.login.dto.RefreshTokenResponse

class FakeAuthApi : AuthApi {
    var authResult: AuthResponse? = null
    var authCalls = mutableListOf<AuthRequest>()

    var refreshResult: RefreshTokenResponse? = null
    var refreshCalls = mutableListOf<RefreshTokenRequest>()

    override suspend fun auth(authRequest: AuthRequest): AuthResponse {
        authCalls += authRequest
        return authResult ?: error("authResult not set")
    }

    override suspend fun refresh(request: RefreshTokenRequest): RefreshTokenResponse {
        refreshCalls += request
        return refreshResult ?: error("refreshResult not set")
    }
}
