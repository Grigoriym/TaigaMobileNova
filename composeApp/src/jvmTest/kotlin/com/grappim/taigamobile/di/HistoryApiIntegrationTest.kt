package com.grappim.taigamobile.di

import com.grappim.taigamobile.feature.history.data.HistoryApi
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Real read round-trip against a live Taiga server: after logging in (see
 * [liveTaigaSessionOrSkip]), fetches comment history for the local instance's confirmed project 5
 * ("Main project") user story #21 (ref 11, "As a user I want to log in with my credentials")
 * through the real `HistoryApi` -> Ktor/OkHttp client -> response mapping. Only asserts the call
 * succeeds and parses, not any particular comment content — the seeded local instance's data isn't
 * something this repo controls or should assert on.
 *
 * Skipped unless `TAIGA_INTEGRATION_URL` / `_USERNAME` / `_PASSWORD` are set.
 */
internal class HistoryApiIntegrationTest {

    @Test
    fun `getCommonTaskComments for user story 21 succeeds`() {
        val koin = liveTaigaSessionOrSkip() ?: return

        val historyApi = koin.get<HistoryApi>()

        val comments = runBlocking {
            historyApi.getCommonTaskComments(singularTaskPath = "userstory", id = 21)
        }

        assertNotNull(comments, "getCommonTaskComments should return a parsed list")
    }
}
