package com.grappim.taigamobile.di

import com.grappim.taigamobile.core.domain.UntrustedCertificateNetworkException
import com.grappim.taigamobile.core.storage.cert.TrustedCertStorage
import com.grappim.taigamobile.feature.login.domain.model.AuthData
import com.grappim.taigamobile.feature.login.domain.model.AuthType
import com.grappim.taigamobile.feature.login.domain.repo.AuthRepository
import kotlinx.coroutines.runBlocking
import org.koin.core.Koin
import kotlin.test.assertTrue

/**
 * Shared setup for `jvmTest` integration tests against a live Taiga server: builds the real
 * [KoinApp] graph and logs in through the real `AuthRepository` -> `AuthApiImpl` -> Ktor/OkHttp
 * client, mirroring `LoginViewModel`'s trust-on-first-use retry for a self-signed certificate.
 *
 * Returns `null` unless `TAIGA_INTEGRATION_URL` / `_USERNAME` / `_PASSWORD` are all set, so every
 * caller stays a clean no-op on CI and any machine without a reachable server. On a login failure
 * with the env vars present, this fails the calling test via [assertTrue] rather than returning
 * null, so a broken server/credentials shows up as a test failure, not a silent skip.
 *
 * **The graph and login only happen once per test JVM, not once per call.** The JVM `DataStore`
 * backends (`StorageModule.jvm.kt`) read/write fixed files under `java.io.tmpdir`, so a second
 * `koinApplication<KoinApp>` in the same process throws "multiple DataStores active for the same
 * file" the moment it touches one. All integration tests in this test run share the one logged-in
 * [Koin] instance below instead — built from [sharedTestKoinGraph], the same graph `KoinGraphTest`
 * uses, so the two never race to build their own (docs/revisit.md #24).
 */
internal fun liveTaigaSessionOrSkip(): Koin? {
    val server = System.getenv("TAIGA_INTEGRATION_URL") ?: return null
    val username = System.getenv("TAIGA_INTEGRATION_USERNAME") ?: return null
    val password = System.getenv("TAIGA_INTEGRATION_PASSWORD") ?: return null

    return sharedSession.value
}

private val sharedSession: Lazy<Koin> = lazy {
    val server = requireNotNull(System.getenv("TAIGA_INTEGRATION_URL"))
    val username = requireNotNull(System.getenv("TAIGA_INTEGRATION_USERNAME"))
    val password = requireNotNull(System.getenv("TAIGA_INTEGRATION_PASSWORD"))

    val koin = sharedTestKoinGraph
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
    koin
}
