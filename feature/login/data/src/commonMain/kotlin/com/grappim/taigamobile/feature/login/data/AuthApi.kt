package com.grappim.taigamobile.feature.login.data

import com.grappim.taigamobile.core.api.AuthHttpClient
import com.grappim.taigamobile.feature.login.dto.AuthRequest
import com.grappim.taigamobile.feature.login.dto.AuthResponse
import com.grappim.taigamobile.feature.login.dto.RefreshTokenRequest
import com.grappim.taigamobile.feature.login.dto.RefreshTokenResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.koin.core.annotation.Single

@Single
class AuthApi(@param:AuthHttpClient private val authClient: HttpClient) {
    suspend fun auth(authRequest: AuthRequest): AuthResponse = authClient.post("auth") {
        setBody(authRequest)
    }.body()

    suspend fun refresh(request: RefreshTokenRequest): RefreshTokenResponse = authClient.post("auth/refresh") {
        setBody(request)
    }.body()
}
