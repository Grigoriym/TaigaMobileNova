package com.grappim.taigamobile.di

import com.grappim.taigamobile.feature.wiki.data.WikiApi
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Real read round-trip against a live Taiga server: after logging in (see
 * [liveTaigaSessionOrSkip]), fetches wiki pages for the local instance's confirmed project 5
 * ("Main project") through the real `WikiApi` -> Ktor/OkHttp client -> response mapping. Only
 * asserts the call succeeds and parses, not any particular page content — the seeded local
 * instance's data isn't something this repo controls or should assert on.
 *
 * Skipped unless `TAIGA_INTEGRATION_URL` / `_USERNAME` / `_PASSWORD` are set.
 */
internal class WikiApiIntegrationTest {

    @Test
    fun `getProjectWikiPages for project 5 succeeds`() {
        val koin = liveTaigaSessionOrSkip() ?: return

        val wikiApi = koin.get<WikiApi>()

        val wikiPages = runBlocking {
            wikiApi.getProjectWikiPages(projectId = 5)
        }

        assertNotNull(wikiPages, "getProjectWikiPages should return a parsed list")
    }
}
