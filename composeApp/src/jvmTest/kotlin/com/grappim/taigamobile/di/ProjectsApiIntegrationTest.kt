package com.grappim.taigamobile.di

import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.projects.data.ProjectsApi
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Real read round-trip against a live Taiga server: after logging in (see
 * [liveTaigaSessionOrSkip]), lists the logged-in user's projects through the real `ProjectsApi` ->
 * Ktor/OkHttp client -> response mapping. Only asserts the call succeeds and parses, not any
 * particular project count or content — the seeded local instance's data isn't something this
 * repo controls or should assert on.
 *
 * Skipped unless `TAIGA_INTEGRATION_URL` / `_USERNAME` / `_PASSWORD` are set.
 */
internal class ProjectsApiIntegrationTest {

    @Test
    fun `getProjects for the logged-in user succeeds`() {
        val koin = liveTaigaSessionOrSkip() ?: return

        val projectsApi = koin.get<ProjectsApi>()
        val sessionStorage = koin.get<TaigaSessionStorage>()

        val projects = runBlocking {
            val memberId = sessionStorage.requireUserId()
            projectsApi.getProjects(memberId = memberId)
        }

        assertNotNull(projects, "getProjects should return a parsed (possibly empty) list")
    }
}
