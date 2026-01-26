package com.grappim.taigamobile.feature.projects.data

import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.db.dao.ProjectDao
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.feature.projects.dto.tags.CreateTagRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.DeleteTagRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.EditTagRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.MixTagsRequestDTO
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.testing.getProject
import com.grappim.taigamobile.testing.getProjectDTO
import com.grappim.taigamobile.testing.getProjectEntity
import com.grappim.taigamobile.testing.getProjectSimple
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.getTag
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ProjectsRepositoryImplTest {
    private val projectsApi = mockk<ProjectsApi>()
    private val projectMapper = mockk<ProjectMapper>()

    private val projectDao = mockk<ProjectDao>()
    private val tagsMapper = mockk<TagsMapper>()
    private val taigaSessionStorage = mockk<TaigaSessionStorage>()

    private val sut: ProjectsRepository = ProjectsRepositoryImpl(
        projectsApi = projectsApi,
        projectMapper = projectMapper,
        projectDao = projectDao,
        taigaSessionStorage = taigaSessionStorage,
        tagsMapper = tagsMapper
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

    @Test
    fun `on saveProject maps and inserts project to dao`() = runTest {
        val project = getProject()
        val entity = getProjectEntity()

        coEvery { projectMapper.toEntity(project) } returns entity
        coJustRun { projectDao.insert(entity) }

        sut.saveProject(project)

        coVerify { projectMapper.toEntity(project) }
        coVerify { projectDao.insert(entity) }
    }

    @Test
    fun `on getCurrentProjectSimple returns project from dao`() = runTest {
        val projectId = getRandomLong()
        val entity = getProjectEntity()
        val expected = getProjectSimple()

        coEvery { taigaSessionStorage.getCurrentProjectId() } returns projectId
        coEvery { projectDao.getProjectById(projectId) } returns entity
        coEvery { projectMapper.toProjectSimple(entity) } returns expected

        val actual = sut.getCurrentProjectSimple()

        assertEquals(expected, actual)
    }

    @Test
    fun `on getPermissions returns permissions from current project`() = runTest {
        val projectId = getRandomLong()
        val entity = getProjectEntity()
        val permissions = persistentListOf(TaigaPermission.VIEW_PROJECT, TaigaPermission.ADD_US)
        val projectSimple = getProjectSimple().copy(myPermissions = permissions)

        coEvery { taigaSessionStorage.getCurrentProjectId() } returns projectId
        coEvery { projectDao.getProjectById(projectId) } returns entity
        coEvery { projectMapper.toProjectSimple(entity) } returns projectSimple

        val actual = sut.getPermissions()

        assertContentEquals(permissions, actual)
    }

    @Test
    fun `on getTagsColors returns tags from api`() = runTest {
        val projectId = getRandomLong()
        val response = mapOf("tag1" to "#FF0000", "tag2" to "#00FF00")
        val expected = listOf(getTag(), getTag())

        coEvery { taigaSessionStorage.getCurrentProjectId() } returns projectId
        coEvery { projectsApi.getProjectTagsColors(projectId) } returns response
        coEvery { tagsMapper.toTags(response) } returns expected.toImmutableList()

        val actual = sut.getTagsColors()

        assertContentEquals(expected, actual)
    }

    @Test
    fun `on deleteTag calls api with correct parameters`() = runTest {
        val projectId = getRandomLong()
        val tagName = getRandomString()

        coEvery { taigaSessionStorage.getCurrentProjectId() } returns projectId
        coJustRun { projectsApi.deleteTag(projectId, any()) }

        sut.deleteTag(tagName)

        coVerify {
            projectsApi.deleteTag(
                projectId = projectId,
                request = DeleteTagRequestDTO(tag = tagName)
            )
        }
    }

    @Test
    fun `on createTag calls api with correct parameters`() = runTest {
        val projectId = getRandomLong()
        val tagName = getRandomString()
        val color = "#FF0000"

        coEvery { taigaSessionStorage.getCurrentProjectId() } returns projectId
        coJustRun { projectsApi.createTag(projectId, any()) }

        sut.createTag(tagName, color)

        coVerify {
            projectsApi.createTag(
                projectId = projectId,
                request = CreateTagRequestDTO(color = color, tag = tagName)
            )
        }
    }

    @Test
    fun `on editTag calls api with correct parameters`() = runTest {
        val projectId = getRandomLong()
        val fromTagName = getRandomString()
        val toTagName = getRandomString()
        val color = "#00FF00"

        coEvery { taigaSessionStorage.getCurrentProjectId() } returns projectId
        coJustRun { projectsApi.editTag(projectId, any()) }

        sut.editTag(fromTagName, toTagName, color)

        coVerify {
            projectsApi.editTag(
                projectId = projectId,
                request = EditTagRequestDTO(fromTag = fromTagName, toTag = toTagName, color = color)
            )
        }
    }

    @Test
    fun `on editTag with null toTagName calls api correctly`() = runTest {
        val projectId = getRandomLong()
        val fromTagName = getRandomString()
        val color = "#00FF00"

        coEvery { taigaSessionStorage.getCurrentProjectId() } returns projectId
        coJustRun { projectsApi.editTag(projectId, any()) }

        sut.editTag(fromTagName, null, color)

        coVerify {
            projectsApi.editTag(
                projectId = projectId,
                request = EditTagRequestDTO(fromTag = fromTagName, toTag = null, color = color)
            )
        }
    }

    @Test
    fun `on mixTags calls api with correct parameters`() = runTest {
        val projectId = getRandomLong()
        val fromTags = listOf(getRandomString(), getRandomString())
        val toTag = getRandomString()

        coEvery { taigaSessionStorage.getCurrentProjectId() } returns projectId
        coJustRun { projectsApi.mixTags(projectId, any()) }

        sut.mixTags(fromTags, toTag)

        coVerify {
            projectsApi.mixTags(
                projectId = projectId,
                request = MixTagsRequestDTO(fromTags = fromTags, toTag = toTag)
            )
        }
    }

    @Test
    fun `on fetchAndSaveProjectInfo fetches and saves project`() = runTest {
        val userId = getRandomLong()
        val projectId = getRandomLong()
        val dto = getProjectDTO().copy(id = projectId)
        val entity = getProjectEntity().copy(id = projectId)

        coEvery { taigaSessionStorage.requireUserId() } returns userId
        coEvery { taigaSessionStorage.getCurrentProjectId() } returns projectId
        coEvery { projectsApi.getProjects(memberId = userId) } returns listOf(dto)
        coEvery { projectMapper.toEntity(dto) } returns entity
        coJustRun { projectDao.insert(entity) }

        sut.fetchAndSaveProjectInfo()

        coVerify { projectDao.insert(entity) }
    }
}
