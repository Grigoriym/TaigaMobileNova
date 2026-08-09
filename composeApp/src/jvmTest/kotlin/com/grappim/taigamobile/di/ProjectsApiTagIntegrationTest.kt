package com.grappim.taigamobile.di

import com.grappim.taigamobile.feature.projects.data.ProjectsApi
import com.grappim.taigamobile.feature.projects.dto.tags.CreateTagRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.DeleteTagRequestDTO
import com.grappim.taigamobile.testing.utils.getRandomString
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Real write round-trip against a live Taiga server: creates a tag on project 5 through the real
 * `ProjectsApi` -> Ktor/OkHttp client -> response mapping, confirms it exists via
 * `getProjectTagsColors`, then deletes it and confirms it's gone. The tag name is randomized per
 * run to avoid colliding with project 5's existing tags, and cleanup also runs in a `finally` so a
 * failed assertion after creation doesn't leave the tag behind in gregory's seeded instance.
 *
 * Skipped unless `TAIGA_INTEGRATION_URL` / `_USERNAME` / `_PASSWORD` are set.
 */
internal class ProjectsApiTagIntegrationTest {

    @Test
    fun `createTag then deleteTag round-trips through the real project`() {
        val koin = liveTaigaSessionOrSkip() ?: return

        val projectsApi = koin.get<ProjectsApi>()
        val projectId = 5L
        val tagName = "int-test-${getRandomString()}"

        runBlocking {
            var created = false
            try {
                projectsApi.createTag(projectId, CreateTagRequestDTO(tag = tagName, color = "#123456"))
                created = true

                val afterCreate = projectsApi.getProjectTagsColors(projectId)
                assertTrue(tagName in afterCreate, "expected '$tagName' to appear after createTag: $afterCreate")

                projectsApi.deleteTag(projectId, DeleteTagRequestDTO(tag = tagName))
                created = false

                val afterDelete = projectsApi.getProjectTagsColors(projectId)
                assertFalse(tagName in afterDelete, "expected '$tagName' to be gone after deleteTag: $afterDelete")
            } finally {
                if (created) {
                    projectsApi.deleteTag(projectId, DeleteTagRequestDTO(tag = tagName))
                }
            }
        }
    }
}
