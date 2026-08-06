package com.grappim.taigamobile.testing.api

import com.grappim.taigamobile.feature.login.data.AuthApi
import com.grappim.taigamobile.feature.login.dto.AuthRequest
import com.grappim.taigamobile.feature.login.dto.AuthResponse
import com.grappim.taigamobile.feature.login.dto.GithubAuthRequest
import com.grappim.taigamobile.feature.login.dto.RefreshTokenRequest
import com.grappim.taigamobile.feature.login.dto.RefreshTokenResponse
import com.grappim.taigamobile.feature.login.dto.TaigaConfJson

class FakeAuthApi : AuthApi {
    var authResult: AuthResponse? = null
    var authCalls = mutableListOf<AuthRequest>()

    var refreshResult: RefreshTokenResponse? = null
    var refreshCalls = mutableListOf<RefreshTokenRequest>()

    var confJsonResult: TaigaConfJson? = null
    var confJsonCalls = mutableListOf<String>()
    var confJsonThrows: Throwable? = null

    var githubAuthResult: AuthResponse? = null
    var githubAuthCalls = mutableListOf<GithubAuthRequest>()

    override suspend fun auth(authRequest: AuthRequest): AuthResponse {
        authCalls += authRequest
        return authResult ?: error("authResult not set")
    }

    override suspend fun refresh(request: RefreshTokenRequest): RefreshTokenResponse {
        refreshCalls += request
        return refreshResult ?: error("refreshResult not set")
    }

    override suspend fun getConfJson(serverUrl: String): TaigaConfJson {
        confJsonCalls += serverUrl
        confJsonThrows?.let { throw it }
        return confJsonResult ?: error("confJsonResult not set")
    }

    override suspend fun githubAuth(request: GithubAuthRequest): AuthResponse {
        githubAuthCalls += request
        return githubAuthResult ?: error("githubAuthResult not set")
    }
}
