package com.grappim.taigamobile.data.interceptors

import com.grappim.taigamobile.core.api.ApiConstants
import com.grappim.taigamobile.core.storage.auth.AuthStateManager
import com.grappim.taigamobile.core.storage.auth.AuthStorage
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
    private val authStorage: AuthStorage,
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
        if (hasExceededRetryLimit(response)) {
            logout()
            return null
        }

        val requestAccessToken = extractToken(response)
        val currentAccessToken = authStorage.token

        Timber.d("requestAccessToken : $requestAccessToken ")

        if (requestAccessToken == null) {
            logout()
            return null
        }

        if (requestAccessToken != currentAccessToken) {
            return response.withBearerToken(currentAccessToken)
        }

        return synchronized(lock) {
            handleTokenRefresh(requestAccessToken, response)
        }
    }

    private fun extractToken(response: Response): String? = response.request.header(ApiConstants.AUTHORIZATION)
        ?.removePrefix("${ApiConstants.BEARER} ")

    private fun handleTokenRefresh(oldToken: String, response: Response): Request? {
        val latestToken = authStorage.token
        if (oldToken != latestToken) {
            return response.withBearerToken(latestToken)
        }

        val refreshResponse = try {
            authApi.refresh(RefreshTokenRequest(authStorage.refreshToken)).execute()
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

        authStorage.setAuthCredentials(token = body.authToken, refreshToken = body.refresh)
        return response.withBearerToken(authStorage.token)
    }

    /**
     * Prevent endless loops
     */
    private fun hasExceededRetryLimit(response: Response): Boolean = response.responseCount >= MAX_AUTHENTICATOR_RETRIES

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
