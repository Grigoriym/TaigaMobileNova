package com.grappim.taigamobile.feature.projects.data

import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.testing.getProjectDTO
import com.grappim.taigamobile.testing.getRandomLong
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertContentEquals

class ProjectsRepositoryImplTest {
    private val projectsApi = mockk<ProjectsApi>()
    private val session = mockk<Session>()
    private val projectMapper = mockk<ProjectMapper>()

    private val sut: ProjectsRepository = ProjectsRepositoryImpl(projectsApi, session, projectMapper)

    @Test
    fun `on getMyProjects return projects from api`() = runTest {
        val userId = getRandomLong()
        val expected = listOf(
            getProjectDTO(),
            getProjectDTO()
        )
        every { session.userId } returns userId
        coEvery { projectsApi.getProjects(memberId = userId) } returns expected

        val actual = sut.getMyProjects()

        assert(actual.isNotEmpty())
        assertContentEquals(expected, actual)
    }

    @Test
    fun `on getUserProjects return projects from api`() = runTest {
        val userId = getRandomLong()
        val expected = listOf(
            getProjectDTO(),
            getProjectDTO()
        )

        coEvery { projectsApi.getProjects(memberId = userId) } returns expected

        val actual = sut.getUserProjectsOld(userId)

        assert(actual.isNotEmpty())
        assertContentEquals(expected, actual)
    }
}
