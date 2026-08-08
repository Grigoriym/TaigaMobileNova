package com.grappim.taigamobile.di

import kotlin.test.Test

/**
 * Real integration test: logs into a live Taiga server through the real `AuthRepository` ->
 * `AuthApiImpl` -> Ktor/OkHttp client, the same engine `androidMain` uses
 * (`KmpNetworkConventionPlugin`) — so a pass here is representative of Android's network behaviour
 * without a device or emulator. See `docs/issues/2026-08-08-integration-tests-live-taiga.md`.
 *
 * Skipped unless `TAIGA_INTEGRATION_URL` / `_USERNAME` / `_PASSWORD` are set, so it never runs on
 * CI or another contributor's machine — there is no server for it to reach there. The login +
 * self-signed-cert-trust flow itself lives in [liveTaigaSessionOrSkip], shared with every other
 * integration test in this package.
 */
internal class LoginIntegrationTest {

    @Test
    fun `login against a live Taiga server succeeds`() {
        liveTaigaSessionOrSkip() ?: return
    }
}
