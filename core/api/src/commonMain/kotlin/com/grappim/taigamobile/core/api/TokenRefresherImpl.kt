package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.storage.auth.AuthStateManager
import com.grappim.taigamobile.feature.login.dto.RefreshTokenResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.koin.core.annotation.Single

@Single(binds = [TokenRefresher::class])
class TokenRefresherImpl(
    @param:AuthHttpClient private val authClient: HttpClient,
    private val authStateManager: AuthStateManager
) : TokenRefresher {
    override suspend fun refresh(refreshToken: String): RefreshedTokens {
        val response = authClient.post("auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("refresh" to refreshToken))
        }.body<RefreshTokenResponse>()
        return RefreshedTokens(response.authToken, response.refresh)
    }

    override fun logout() = authStateManager.logout()
}
