package com.grappim.taigamobile.di

import com.grappim.taigamobile.feature.userstories.data.GetUserStoriesParams
import com.grappim.taigamobile.feature.userstories.data.UserStoriesApi
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Real read round-trip against a live Taiga server: after logging in (see
 * [liveTaigaSessionOrSkip]), lists user stories for the local instance's confirmed project 5
 * ("Main project") through the real `UserStoriesApi` -> Ktor/OkHttp client -> response mapping.
 * Only asserts the call succeeds and parses, not any particular story count or content — the
 * seeded local instance's data isn't something this repo controls or should assert on.
 *
 * Skipped unless `TAIGA_INTEGRATION_URL` / `_USERNAME` / `_PASSWORD` are set.
 */
internal class UserStoriesApiIntegrationTest {

    @Test
    fun `getUserStories for project 5 succeeds`() {
        val koin = liveTaigaSessionOrSkip() ?: return

        val userStoriesApi = koin.get<UserStoriesApi>()

        val userStories = runBlocking {
            userStoriesApi.getUserStories(GetUserStoriesParams(project = 5))
        }

        assertNotNull(userStories, "getUserStories should return a parsed (possibly empty) list")
    }
}
