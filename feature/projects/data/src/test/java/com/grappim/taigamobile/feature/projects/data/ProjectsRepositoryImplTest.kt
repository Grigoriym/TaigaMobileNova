package com.grappim.taigamobile.feature.projects.data

import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.db.dao.ProjectDao
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.testing.getProject
import com.grappim.taigamobile.testing.getProjectDTO
import com.grappim.taigamobile.testing.getRandomLong
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertContentEquals

class ProjectsRepositoryImplTest {
    private val projectsApi = mockk<ProjectsApi>()
    private val projectMapper = mockk<ProjectMapper>()

    private val projectDao = mockk<ProjectDao>()
    private val taigaSessionStorage = mockk<TaigaSessionStorage>()

    private val sut: ProjectsRepository = ProjectsRepositoryImpl(
        projectsApi = projectsApi,
        projectMapper = projectMapper,
        projectDao = projectDao,
        taigaSessionStorage = taigaSessionStorage
    )

    @Test
    fun `on getMyProjects return projects from api`() = runTest {
        val userId = getRandomLong()
        val dtos = listOf(
            getProjectDTO(),
            getProjectDTO()
        )
        val expected = listOf(
            getProject(),
            getProject()
        )
        coEvery { taigaSessionStorage.requireUserId() } returns userId
        coEvery { projectsApi.getProjects(memberId = userId) } returns dtos
        coEvery { projectMapper.toListDomain(dtos) } returns expected.toImmutableList()

        val actual = sut.getMyProjects()

        assert(actual.isNotEmpty())
        assertContentEquals(expected, actual)
    }

    @Test
    fun `on getUserProjects return projects from api`() = runTest {
        val userId = getRandomLong()
        val dtos = listOf(
            getProjectDTO(),
            getProjectDTO()
        )
        val expected = listOf(
            getProject(),
            getProject()
        )

        coEvery { projectsApi.getProjects(memberId = userId) } returns dtos
        coEvery { projectMapper.toListDomain(dtos) } returns expected.toImmutableList()

        val actual = sut.getUserProjects(userId)

        assert(actual.isNotEmpty())
        assertContentEquals(expected, actual)
    }
}
