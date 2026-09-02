package com.grappim.taigamobile.di

import com.grappim.taigamobile.feature.swimlanes.data.SwimlanesApi
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Real read round-trip against a live Taiga server: after logging in (see
 * [liveTaigaSessionOrSkip]), fetches swimlanes for the local instance's confirmed project 5
 * ("Main project") through the real `SwimlanesApi` -> Ktor/OkHttp client -> response mapping.
 * Project 5 has zero swimlanes configured, so this asserts the call succeeds and parses an empty
 * list — still a valid round-trip of the request/response path.
 *
 * Skipped unless `TAIGA_INTEGRATION_URL` / `_USERNAME` / `_PASSWORD` are set.
 */
internal class SwimlanesApiIntegrationTest {

    @Test
    fun `getSwimlanes for project 5 succeeds`() {
        val koin = liveTaigaSessionOrSkip() ?: return

        val swimlanesApi = koin.get<SwimlanesApi>()

        val swimlanes = runBlocking {
            swimlanesApi.getSwimlanes(project = 5)
        }

        assertNotNull(swimlanes, "getSwimlanes should return a parsed list")
    }
}
