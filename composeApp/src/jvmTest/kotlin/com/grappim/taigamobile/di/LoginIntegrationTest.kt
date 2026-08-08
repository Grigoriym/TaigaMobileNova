package com.grappim.taigamobile.di

import com.grappim.taigamobile.core.domain.UntrustedCertificateNetworkException
import com.grappim.taigamobile.core.storage.cert.TrustedCertStorage
import com.grappim.taigamobile.feature.login.domain.model.AuthData
import com.grappim.taigamobile.feature.login.domain.model.AuthType
import com.grappim.taigamobile.feature.login.domain.repo.AuthRepository
import kotlinx.coroutines.runBlocking
import org.koin.core.logger.Level
import org.koin.plugin.module.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Real integration test: logs into a live Taiga server through the real `AuthRepository` ->
 * `AuthApiImpl` -> Ktor/OkHttp client, the same engine `androidMain` uses
 * (`KmpNetworkConventionPlugin`) — so a pass here is representative of Android's network behaviour
 * without a device or emulator. See `docs/issues/2026-08-08-integration-tests-live-taiga.md`.
 *
 * Skipped unless `TAIGA_INTEGRATION_URL` / `_USERNAME` / `_PASSWORD` are set, so it never runs on
 * CI or another contributor's machine — there is no server for it to reach there.
 *
 * If the server presents a self-signed certificate, this mirrors `LoginViewModel`'s real
 * trust-on-first-use flow: the first `auth()` call fails with
 * [UntrustedCertificateNetworkException], the cert is trusted, and the request is retried once.
 */
internal class LoginIntegrationTest {

    @Test
    fun `login against a live Taiga server succeeds`() {
        val server = System.getenv("TAIGA_INTEGRATION_URL") ?: return
        val username = System.getenv("TAIGA_INTEGRATION_USERNAME") ?: return
        val password = System.getenv("TAIGA_INTEGRATION_PASSWORD") ?: return

        val koin = koinApplication<KoinApp> { printLogger(Level.NONE) }.koin
        val authRepository = koin.get<AuthRepository>()
        val trustedCertStorage = koin.get<TrustedCertStorage>()

        val authData = AuthData(
            taigaServer = server,
            authType = AuthType.NORMAL,
            username = username,
            password = password
        )

        val result = runBlocking {
            val firstAttempt = authRepository.auth(authData)
            val error = firstAttempt.exceptionOrNull()
            if (error is UntrustedCertificateNetworkException) {
                trustedCertStorage.trust(error.pendingCertTrust)
                authRepository.auth(authData)
            } else {
                firstAttempt
            }
        }

        assertTrue(result.isSuccess, "login failed: ${result.exceptionOrNull()}")
    }
}
