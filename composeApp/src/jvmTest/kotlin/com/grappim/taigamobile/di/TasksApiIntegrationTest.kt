package com.grappim.taigamobile.di

import com.grappim.taigamobile.feature.tasks.data.TasksApi
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Real read round-trip against a live Taiga server: after logging in (see
 * [liveTaigaSessionOrSkip]), lists tasks for the local instance's confirmed project 5
 * ("Main project") through the real `TasksApi` -> Ktor/OkHttp client -> response mapping.
 * Only asserts the call succeeds and parses, not any particular task count or content — the
 * seeded local instance's data isn't something this repo controls or should assert on.
 *
 * Skipped unless `TAIGA_INTEGRATION_URL` / `_USERNAME` / `_PASSWORD` are set.
 */
internal class TasksApiIntegrationTest {

    @Test
    fun `getTasks for project 5 succeeds`() {
        val koin = liveTaigaSessionOrSkip() ?: return

        val tasksApi = koin.get<TasksApi>()

        val tasks = runBlocking {
            tasksApi.getTasks(project = 5)
        }

        assertNotNull(tasks, "getTasks should return a parsed (possibly empty) list")
    }
}
