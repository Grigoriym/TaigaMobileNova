package com.grappim.taigamobile.feature.projects.data

import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.projects.dto.tags.CreateTagRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.DeleteTagRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.EditTagRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.MixTagsRequestDTO
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.testing.api.FakeProjectsApi
import com.grappim.taigamobile.testing.dao.FakeProjectDao
import com.grappim.taigamobile.testing.models.getProject
import com.grappim.taigamobile.testing.models.getProjectDTO
import com.grappim.taigamobile.testing.models.getProjectEntity
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

internal class ProjectsRepositoryImplTest {
    private val projectsApi = FakeProjectsApi()
    private val projectMapper = ProjectMapper()
    private val projectDao = FakeProjectDao()
    private val tagsMapper = TagsMapper()
    private val taigaSessionStorage = FakeTaigaSessionStorage()

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
        val dtos = listOf(getProjectDTO(), getProjectDTO())
        taigaSessionStorage.currentUserId = userId
        projectsApi.getProjectsResult = dtos

        val actual = sut.getMyProjects()

        assertContentEquals(projectMapper.toListDomain(dtos), actual)
    }

    @Test
    fun `on getUserProjects return projects from api`() = runTest {
        val userId = getRandomLong()
        val dtos = listOf(getProjectDTO(), getProjectDTO())
        projectsApi.getProjectsResult = dtos

        val actual = sut.getUserProjects(userId)

        assertContentEquals(projectMapper.toListDomain(dtos), actual)
    }

    @Test
    fun `on saveProject maps and inserts project to dao`() = runTest {
        val project = getProject()
        val expected = projectMapper.toEntity(project)

        sut.saveProject(project)

        assertEquals(expected, projectDao.insertCalls.single())
    }

    @Test
    fun `on getCurrentProjectSimple returns project from dao`() = runTest {
        val entity = getProjectEntity()
        taigaSessionStorage.currentProjectId = entity.id
        projectDao.projectsById[entity.id] = entity

        val actual = sut.getCurrentProjectSimple()

        assertEquals(projectMapper.toProjectSimple(entity), actual)
    }

    @Test
    fun `on getPermissions returns permissions from current project`() = runTest {
        val entity = getProjectEntity()
        taigaSessionStorage.currentProjectId = entity.id
        projectDao.projectsById[entity.id] = entity

        val actual = sut.getPermissions()

        assertContentEquals(entity.myPermissions.toImmutableList(), actual)
    }

    @Test
    fun `on getTagsColors returns tags from api`() = runTest {
        val projectId = getRandomLong()
        val response = mapOf("tag1" to "#FF0000", "tag2" to "#00FF00")
        taigaSessionStorage.currentProjectId = projectId
        projectsApi.tagsColorsResult = response

        val actual = sut.getTagsColors()

        assertContentEquals(tagsMapper.toTags(response), actual)
    }

    @Test
    fun `on deleteTag calls api with correct parameters`() = runTest {
        val projectId = getRandomLong()
        val tagName = getRandomString()
        taigaSessionStorage.currentProjectId = projectId

        sut.deleteTag(tagName)

        val call = projectsApi.deleteTagCalls.single()
        assertEquals(projectId, call.first)
        assertEquals(DeleteTagRequestDTO(tag = tagName), call.second)
    }

    @Test
    fun `on createTag calls api with correct parameters`() = runTest {
        val projectId = getRandomLong()
        val tagName = getRandomString()
        val color = "#FF0000"
        taigaSessionStorage.currentProjectId = projectId

        sut.createTag(tagName, color)

        val call = projectsApi.createTagCalls.single()
        assertEquals(projectId, call.first)
        assertEquals(CreateTagRequestDTO(color = color, tag = tagName), call.second)
    }

    @Test
    fun `on editTag calls api with correct parameters`() = runTest {
        val projectId = getRandomLong()
        val fromTagName = getRandomString()
        val toTagName = getRandomString()
        val color = "#00FF00"
        taigaSessionStorage.currentProjectId = projectId

        sut.editTag(fromTagName, toTagName, color)

        val call = projectsApi.editTagCalls.single()
        assertEquals(projectId, call.first)
        assertEquals(EditTagRequestDTO(fromTag = fromTagName, toTag = toTagName, color = color), call.second)
    }

    @Test
    fun `on editTag with null toTagName calls api correctly`() = runTest {
        val projectId = getRandomLong()
        val fromTagName = getRandomString()
        val color = "#00FF00"
        taigaSessionStorage.currentProjectId = projectId

        sut.editTag(fromTagName, null, color)

        val call = projectsApi.editTagCalls.single()
        assertEquals(projectId, call.first)
        assertEquals(EditTagRequestDTO(fromTag = fromTagName, toTag = null, color = color), call.second)
    }

    @Test
    fun `on mixTags calls api with correct parameters`() = runTest {
        val projectId = getRandomLong()
        val fromTags = listOf(getRandomString(), getRandomString())
        val toTag = getRandomString()
        taigaSessionStorage.currentProjectId = projectId

        sut.mixTags(fromTags, toTag)

        val call = projectsApi.mixTagsCalls.single()
        assertEquals(projectId, call.first)
        assertEquals(MixTagsRequestDTO(fromTags = fromTags, toTag = toTag), call.second)
    }

    @Test
    fun `on fetchAndSaveProjectInfo fetches and saves project`() = runTest {
        val userId = getRandomLong()
        val projectId = getRandomLong()
        val dto = getProjectDTO().copy(id = projectId)
        val expected = projectMapper.toEntity(dto)
        taigaSessionStorage.currentUserId = userId
        taigaSessionStorage.currentProjectId = projectId
        projectsApi.getProjectsResult = listOf(dto)

        sut.fetchAndSaveProjectInfo()

        assertEquals(expected, projectDao.insertCalls.single())
    }
}
