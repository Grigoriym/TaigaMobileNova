package com.grappim.taigamobile.di

import com.grappim.taigamobile.feature.projects.data.ProjectValuesApi
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Real read round-trip against a live Taiga server: after logging in (see
 * [liveTaigaSessionOrSkip]), fetches user story statuses for the local instance's confirmed
 * project 5 ("Main project") through the real `ProjectValuesApi` -> Ktor/OkHttp client -> response
 * mapping. Only asserts the call succeeds and parses, not any particular status content — the
 * seeded local instance's data isn't something this repo controls or should assert on.
 *
 * Skipped unless `TAIGA_INTEGRATION_URL` / `_USERNAME` / `_PASSWORD` are set.
 */
internal class ProjectValuesApiIntegrationTest {

    @Test
    fun `getProjectValues for project 5 userstory-statuses succeeds`() {
        val koin = liveTaigaSessionOrSkip() ?: return

        val projectValuesApi = koin.get<ProjectValuesApi>()

        val values = runBlocking {
            projectValuesApi.getProjectValues(endpoint = "userstory-statuses", projectId = 5)
        }

        assertNotNull(values, "getProjectValues should return parsed project values")
    }
}
