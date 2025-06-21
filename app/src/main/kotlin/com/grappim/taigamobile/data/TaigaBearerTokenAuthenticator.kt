package com.grappim.taigamobile.data

import com.grappim.taigamobile.core.api.ApiConstants
import com.grappim.taigamobile.core.storage.AuthStateManager
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.login.data.api.AuthApi
import com.grappim.taigamobile.feature.login.data.model.RefreshTokenRequest
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A reactive authentication where we cal a refresh token request to get new token
 * In case of an error we logout the user
 */
@Singleton
class TaigaBearerTokenAuthenticator @Inject constructor(
    private val session: Session,
    private val authApi: AuthApi,
    private val authStateManager: AuthStateManager
) : Authenticator {

    companion object {
        /**
         * Prevents infinite authenticator loops (e.g. if refresh repeatedly fails)
         * Took it from here: https://square.github.io/okhttp/recipes/#handling-authentication-kt-java
         */
        private const val MAX_AUTHENTICATOR_RETRIES = 3
    }

    private val lock = Any()

    private val Response.responseCount: Int
        get() = generateSequence(this) { it.priorResponse }.count()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent endless loops
        if (response.responseCount >= MAX_AUTHENTICATOR_RETRIES) {
            logout()
            return null
        }

        val requestAccessToken = response.request.header(ApiConstants.AUTHORIZATION)
            ?.removePrefix("${ApiConstants.BEARER} ")
        val currentAccessToken = session.token.value

        Timber.d("requestAccessToken : $requestAccessToken ")

        // Another thread has already refreshed the token, so we will just retry with the new one
        // This can happen when several requests are sent to the server at the same time
        if (requestAccessToken != null && requestAccessToken != currentAccessToken) {
            return response.withBearerToken(currentAccessToken)
        }

        if (requestAccessToken == null) {
            logout()
            return null
        }

        synchronized(lock) {
            // Double-check if token was refreshed while we waited for the lock
            val newAccessToken = session.token.value
            if (requestAccessToken != newAccessToken) {
                return response.withBearerToken(newAccessToken)
            }

            val refreshRequest = RefreshTokenRequest(session.refreshToken.value)

            val refreshResponse = try {
                authApi.refresh(request = refreshRequest).execute()
            } catch (e: Exception) {
                Timber.e(e)
                logout()
                return null
            }
            val body = refreshResponse.body()
            if (!refreshResponse.isSuccessful || body == null) {
                logout()
                return null
            }

            session.changeAuthCredentials(
                body.authToken,
                body.refresh
            )
            return response.withBearerToken(session.token.value)
        }
    }

    private fun Response.withBearerToken(newToken: String): Request = request.newBuilder()
        .header(
            ApiConstants.AUTHORIZATION,
            ApiConstants.generateBearerToken(newToken)
        )
        .build()

    private fun logout() {
        authStateManager.logout()
    }
}
