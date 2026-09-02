package com.grappim.taigamobile.di

import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Real read round-trip against a live Taiga server: after logging in (see
 * [liveTaigaSessionOrSkip]), fetches epics for the local instance's confirmed project 5
 * ("Main project") through the real `WorkItemApi` -> Ktor/OkHttp client -> response mapping. This
 * is also where `EpicsApi`/`IssuesApi` reads actually live — both are write-only interfaces (see
 * `docs/testing/integration-tests-plan.md` task 3). Only asserts the call succeeds and parses, not
 * any particular epic content — the seeded local instance's data isn't something this repo controls
 * or should assert on.
 *
 * Skipped unless `TAIGA_INTEGRATION_URL` / `_USERNAME` / `_PASSWORD` are set.
 */
internal class WorkItemApiIntegrationTest {

    @Test
    fun `getWorkItems for epics in project 5 succeeds`() {
        val koin = liveTaigaSessionOrSkip() ?: return

        val workItemApi = koin.get<WorkItemApi>()

        val epics = runBlocking {
            workItemApi.getWorkItems(taskPath = "epics", project = 5)
        }

        assertNotNull(epics, "getWorkItems should return a parsed list")
    }
}
