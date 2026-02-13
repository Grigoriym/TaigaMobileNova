package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.logger.LogPriority
import com.grappim.taigamobile.core.logger.logcat
import com.grappim.taigamobile.core.storage.auth.AuthStorage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.http.HttpStatusCode
import io.ktor.util.AttributeKey
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Result of a token refresh call.
 */
data class RefreshedTokens(
    val authToken: String,
    val refreshToken: String
)

/**
 * Ktor plugin that handles 401 responses by refreshing the auth token.
 * Replaces OkHttp's TaigaBearerTokenAuthenticator.
 */
class TokenRefreshPlugin(
    private val authStorage: AuthStorage,
    private val tokenRefresher: TokenRefresher
) {

    class Config {
        lateinit var authStorage: AuthStorage
        lateinit var tokenRefresher: TokenRefresher
    }

    companion object Plugin : HttpClientPlugin<Config, TokenRefreshPlugin> {
        override val key: AttributeKey<TokenRefreshPlugin> = AttributeKey("TokenRefreshPlugin")

        private const val MAX_RETRIES = 3

        private val retryCountKey = AttributeKey<Int>("TokenRefreshRetryCount")

        override fun prepare(block: Config.() -> Unit): TokenRefreshPlugin {
            val config = Config().apply(block)
            return TokenRefreshPlugin(
                authStorage = config.authStorage,
                tokenRefresher = config.tokenRefresher
            )
        }

        override fun install(plugin: TokenRefreshPlugin, scope: HttpClient) {
            val mutex = Mutex()

            scope.plugin(HttpSend).intercept { request ->
                val response = execute(request)

                if (response.response.status != HttpStatusCode.Unauthorized) {
                    return@intercept response
                }

                val retries = request.attributes.getOrNull(retryCountKey) ?: 0
                if (retries >= MAX_RETRIES) {
                    logcat(tag = "TokenRefreshPlugin") {
                        "Max retries ($MAX_RETRIES) exceeded, logging out"
                    }
                    plugin.tokenRefresher.logout()
                    return@intercept response
                }

                val requestToken = request.headers[ApiConstants.AUTHORIZATION]
                    ?.removePrefix("${ApiConstants.BEARER} ")

                if (requestToken == null) {
                    plugin.tokenRefresher.logout()
                    return@intercept response
                }

                val currentToken = plugin.authStorage.getToken()

                // Another coroutine already refreshed the token
                if (requestToken != currentToken) {
                    request.headers.remove(ApiConstants.AUTHORIZATION)
                    request.headers.append(
                        ApiConstants.AUTHORIZATION,
                        ApiConstants.generateBearerToken(currentToken)
                    )
                    request.attributes.put(retryCountKey, retries + 1)
                    return@intercept execute(request)
                }

                // We need to refresh — use mutex to prevent concurrent refresh calls
                mutex.withLock {
                    // Double-check: another coroutine may have refreshed while we waited
                    val latestToken = plugin.authStorage.getToken()
                    if (requestToken != latestToken) {
                        request.headers.remove(ApiConstants.AUTHORIZATION)
                        request.headers.append(
                            ApiConstants.AUTHORIZATION,
                            ApiConstants.generateBearerToken(latestToken)
                        )
                        request.attributes.put(retryCountKey, retries + 1)
                        return@withLock execute(request)
                    }

                    val refreshed = try {
                        val currentRefresh = plugin.authStorage.getRefreshToken()
                        plugin.tokenRefresher.refresh(currentRefresh)
                    } catch (e: Exception) {
                        logcat(
                            priority = LogPriority.ERROR,
                            tag = "TokenRefreshPlugin",
                            throwable = e
                        ) { "Token refresh failed" }
                        plugin.tokenRefresher.logout()
                        return@withLock response
                    }

                    plugin.authStorage.setAuthCredentials(
                        token = refreshed.authToken,
                        refreshToken = refreshed.refreshToken
                    )

                    request.headers.remove(ApiConstants.AUTHORIZATION)
                    request.headers.append(
                        ApiConstants.AUTHORIZATION,
                        ApiConstants.generateBearerToken(refreshed.authToken)
                    )
                    request.attributes.put(retryCountKey, retries + 1)
                    execute(request)
                }
            }
        }
    }
}
