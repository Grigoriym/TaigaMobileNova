package com.grappim.taigamobile.di

import com.grappim.taigamobile.feature.sprint.data.SprintApi
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Real read round-trip against a live Taiga server: after logging in (see
 * [liveTaigaSessionOrSkip]), lists open sprints for the local instance's confirmed project 5
 * ("Main project") through the real `SprintApi` -> Ktor/OkHttp client -> response mapping.
 * Only asserts the call succeeds and parses, not any particular sprint count or content — the
 * seeded local instance's data isn't something this repo controls or should assert on.
 *
 * Skipped unless `TAIGA_INTEGRATION_URL` / `_USERNAME` / `_PASSWORD` are set.
 */
internal class SprintApiIntegrationTest {

    @Test
    fun `getSprints for project 5 succeeds`() {
        val koin = liveTaigaSessionOrSkip() ?: return

        val sprintApi = koin.get<SprintApi>()

        val sprints = runBlocking {
            sprintApi.getSprints(project = 5, isClosed = false)
        }

        assertNotNull(sprints, "getSprints should return a parsed (possibly empty) list")
    }
}
