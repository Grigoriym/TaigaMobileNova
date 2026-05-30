package com.grappim.taigamobile.feature.login.data

import com.grappim.taigamobile.core.api.AuthHttpClient
import com.grappim.taigamobile.feature.login.dto.AuthRequest
import com.grappim.taigamobile.feature.login.dto.AuthResponse
import com.grappim.taigamobile.feature.login.dto.GithubAuthRequest
import com.grappim.taigamobile.feature.login.dto.RefreshTokenRequest
import com.grappim.taigamobile.feature.login.dto.RefreshTokenResponse
import com.grappim.taigamobile.feature.login.dto.TaigaConfJson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.koin.core.annotation.Single

interface AuthApi {
    suspend fun auth(authRequest: AuthRequest): AuthResponse
    suspend fun refresh(request: RefreshTokenRequest): RefreshTokenResponse
    suspend fun getConfJson(serverUrl: String): TaigaConfJson
    suspend fun githubAuth(request: GithubAuthRequest): AuthResponse
}

@Single(binds = [AuthApi::class])
class AuthApiImpl(@param:AuthHttpClient private val authClient: HttpClient) : AuthApi {
    override suspend fun auth(authRequest: AuthRequest): AuthResponse = authClient.post("auth") {
        setBody(authRequest)
    }.body()

    override suspend fun refresh(request: RefreshTokenRequest): RefreshTokenResponse = authClient.post("auth/refresh") {
        setBody(request)
    }.body()

    override suspend fun getConfJson(serverUrl: String): TaigaConfJson = authClient.get("$serverUrl/conf.json").body()

    override suspend fun githubAuth(request: GithubAuthRequest): AuthResponse = authClient.post("auth") {
        setBody(request)
    }.body()
}
