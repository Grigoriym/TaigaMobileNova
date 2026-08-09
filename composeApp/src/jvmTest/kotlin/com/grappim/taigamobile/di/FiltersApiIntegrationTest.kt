package com.grappim.taigamobile.di

import com.grappim.taigamobile.feature.filters.data.FiltersApi
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Real read round-trip against a live Taiga server: after logging in (see
 * [liveTaigaSessionOrSkip]), fetches user story filters data for the local instance's confirmed
 * project 5 ("Main project") through the real `FiltersApi` -> Ktor/OkHttp client -> response
 * mapping. Only asserts the call succeeds and parses, not any particular filter content — the
 * seeded local instance's data isn't something this repo controls or should assert on.
 *
 * Skipped unless `TAIGA_INTEGRATION_URL` / `_USERNAME` / `_PASSWORD` are set.
 */
internal class FiltersApiIntegrationTest {

    @Test
    fun `getCommonTaskFiltersData for project 5 userstories succeeds`() {
        val koin = liveTaigaSessionOrSkip() ?: return

        val filtersApi = koin.get<FiltersApi>()

        val filtersData = runBlocking {
            filtersApi.getCommonTaskFiltersData(taskPath = "userstories", project = 5)
        }

        assertNotNull(filtersData, "getCommonTaskFiltersData should return parsed filters data")
    }
}
