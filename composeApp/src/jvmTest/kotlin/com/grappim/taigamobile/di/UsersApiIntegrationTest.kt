package com.grappim.taigamobile.di

import com.grappim.taigamobile.feature.users.data.UsersApi
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Real read round-trip against a live Taiga server: after logging in (see
 * [liveTaigaSessionOrSkip]), fetches the logged-in user's own profile through the real `UsersApi`
 * -> Ktor/OkHttp client -> response mapping. Only asserts the call succeeds and parses, not any
 * particular profile content — the seeded local instance's data isn't something this repo controls
 * or should assert on.
 *
 * Skipped unless `TAIGA_INTEGRATION_URL` / `_USERNAME` / `_PASSWORD` are set.
 */
internal class UsersApiIntegrationTest {

    @Test
    fun `getMyProfile for the logged-in user succeeds`() {
        val koin = liveTaigaSessionOrSkip() ?: return

        val usersApi = koin.get<UsersApi>()

        val profile = runBlocking { usersApi.getMyProfile() }

        assertNotNull(profile, "getMyProfile should return a parsed profile")
    }
}
