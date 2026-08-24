package com.grappim.taigamobile.feature.projects.data

import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.projects.dto.UpdateModulesRequestDTO
import com.grappim.taigamobile.feature.projects.dto.UpdateProjectRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.CreateTagRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.DeleteTagRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.EditTagRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.MixTagsRequestDTO
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.testing.api.FakeProjectsApi
import com.grappim.taigamobile.testing.dao.FakeProjectDao
import com.grappim.taigamobile.testing.models.getProject
import com.grappim.taigamobile.testing.models.getProjectDTO
import com.grappim.taigamobile.testing.models.getProjectDetailDTO
import com.grappim.taigamobile.testing.models.getProjectEntity
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.assertFailsWithTestException
import com.grappim.taigamobile.testing.utils.getRandomBoolean
import com.grappim.taigamobile.testing.utils.getRandomInt
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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

    @Test
    fun `on fetchAndSaveProjectInfo throws when current project is not among user projects`() = runTest {
        taigaSessionStorage.currentUserId = getRandomLong()
        taigaSessionStorage.currentProjectId = getRandomLong()
        projectsApi.getProjectsResult = listOf(getProjectDTO(), getProjectDTO())

        val thrown = assertFailsWith<IllegalStateException> { sut.fetchAndSaveProjectInfo() }

        assertEquals("Something is not right", thrown.message)
        assertTrue(projectDao.insertCalls.isEmpty())
    }

    /**
     * Only asserts that the [androidx.paging.Pager] is constructed — the flow is deliberately not
     * collected. Collecting it would run [ProjectsPagingSource] against [FakeProjectsApi], whose
     * `getProjectsPaging` cannot return an `HttpResponse`; the escaping exception would then be
     * reported against an unrelated test (see CLAUDE.md, Testing). `ProjectsPagingSource` is
     * covered by task 9b's decision on how to produce a real `HttpResponse`.
     */
    @Test
    fun `on fetchProjects returns a paging flow`() = runTest {
        taigaSessionStorage.currentUserId = getRandomLong()

        assertNotNull(sut.fetchProjects(getRandomString()))
    }

    @Test
    fun `on getCurrentProjectFlow emits mapped project for the current project id`() = runTest {
        val entity = getProjectEntity()
        taigaSessionStorage.setCurrentProjectId(entity.id)
        projectDao.projectFlowsById[entity.id] = listOf(entity)

        val actual = sut.getCurrentProjectFlow().first()

        assertEquals(projectMapper.toProjectSimple(entity), actual)
        assertEquals(entity.id, projectDao.getProjectByIdFlowCalls.single())
    }

    @Test
    fun `on getCurrentProjectFlow skips null entities`() = runTest {
        val entity = getProjectEntity()
        taigaSessionStorage.setCurrentProjectId(entity.id)
        projectDao.projectFlowsById[entity.id] = listOf(null, entity)

        val actual = sut.getCurrentProjectFlow().first()

        assertEquals(projectMapper.toProjectSimple(entity), actual)
    }

    @Test
    fun `on getProjectDetails returns mapped details from api`() = runTest {
        val projectId = getRandomLong()
        val dto = getProjectDetailDTO()
        taigaSessionStorage.currentProjectId = projectId
        projectsApi.projectDetailDTO = dto

        val actual = sut.getProjectDetails()

        assertEquals(projectMapper.toProjectDetails(dto), actual)
        assertEquals(projectId, projectsApi.getProjectDetailCalls.single())
    }

    @Test
    fun `on getProjectModules returns mapped modules from api`() = runTest {
        val projectId = getRandomLong()
        val dto = getProjectDetailDTO()
        taigaSessionStorage.currentProjectId = projectId
        projectsApi.projectDetailDTO = dto

        val actual = sut.getProjectModules()

        assertEquals(projectMapper.toProjectModules(dto), actual)
        assertEquals(projectId, projectsApi.getProjectDetailCalls.single())
    }

    @Test
    fun `on updateModules calls api with correct parameters and saves the response`() = runTest {
        val projectId = getRandomLong()
        val dto = getProjectDetailDTO()
        val totalMilestones = getRandomInt()
        val totalStoryPoints = 12.5
        taigaSessionStorage.currentProjectId = projectId
        projectsApi.projectDetailDTO = dto

        sut.updateModules(
            isEpicsActivated = true,
            isBacklogActivated = false,
            isKanbanActivated = true,
            isIssuesActivated = false,
            isWikiActivated = true,
            totalMilestones = totalMilestones,
            totalStoryPoints = totalStoryPoints
        )

        val call = projectsApi.updateModulesCalls.single()
        assertEquals(projectId, call.first)
        assertEquals(
            UpdateModulesRequestDTO(
                isEpicsActivated = true,
                isBacklogActivated = false,
                isKanbanActivated = true,
                isIssuesActivated = false,
                isWikiActivated = true,
                totalMilestones = totalMilestones,
                totalStoryPoints = totalStoryPoints
            ),
            call.second
        )
        assertEquals(projectMapper.toEntity(dto), projectDao.insertCalls.single())
    }

    @Test
    fun `on updateProject calls api with correct parameters and saves the response`() = runTest {
        val projectId = getRandomLong()
        val dto = getProjectDetailDTO()
        val name = getRandomString()
        val description = getRandomString()
        val note = getRandomString()
        taigaSessionStorage.currentProjectId = projectId
        projectsApi.projectDetailDTO = dto

        sut.updateProject(
            name = name,
            description = description,
            isPrivate = true,
            isLookingForPeople = false,
            lookingForPeopleNote = note,
            isContactActivated = true
        )

        val call = projectsApi.updateProjectCalls.single()
        assertEquals(projectId, call.first)
        assertEquals(
            UpdateProjectRequestDTO(
                name = name,
                description = description,
                isPrivate = true,
                isLookingForPeople = false,
                lookingForPeopleNote = note,
                isContactActivated = true
            ),
            call.second
        )
        assertEquals(projectMapper.toEntity(dto), projectDao.insertCalls.single())
    }

    // Failure paths — one per public method that touches a collaborator. See CLAUDE.md, Testing.

    @Test
    fun `on getMyProjects error propagates`() = runTest {
        taigaSessionStorage.currentUserId = getRandomLong()
        projectsApi.errorToThrow = testException

        assertFailsWithTestException { sut.getMyProjects() }
    }

    @Test
    fun `on getUserProjects error propagates`() = runTest {
        projectsApi.errorToThrow = testException

        assertFailsWithTestException { sut.getUserProjects(getRandomLong()) }
    }

    @Test
    fun `on fetchAndSaveProjectInfo api error propagates`() = runTest {
        taigaSessionStorage.currentUserId = getRandomLong()
        projectsApi.errorToThrow = testException

        assertFailsWithTestException { sut.fetchAndSaveProjectInfo() }
    }

    @Test
    fun `on saveProject dao error propagates`() = runTest {
        projectDao.errorToThrow = testException

        assertFailsWithTestException { sut.saveProject(getProject()) }
    }

    @Test
    fun `on getCurrentProjectSimple dao error propagates`() = runTest {
        projectDao.errorToThrow = testException

        assertFailsWithTestException { sut.getCurrentProjectSimple() }
    }

    @Test
    fun `on getCurrentProjectFlow dao error propagates`() = runTest {
        taigaSessionStorage.setCurrentProjectId(getRandomLong())
        projectDao.errorToThrow = testException

        assertFailsWithTestException { sut.getCurrentProjectFlow().first() }
    }

    @Test
    fun `on getPermissions dao error propagates`() = runTest {
        projectDao.errorToThrow = testException

        assertFailsWithTestException { sut.getPermissions() }
    }

    @Test
    fun `on getProjectDetails error propagates`() = runTest {
        projectsApi.errorToThrow = testException

        assertFailsWithTestException { sut.getProjectDetails() }
    }

    @Test
    fun `on getProjectModules error propagates`() = runTest {
        projectsApi.errorToThrow = testException

        assertFailsWithTestException { sut.getProjectModules() }
    }

    @Test
    fun `on updateModules error propagates`() = runTest {
        projectsApi.errorToThrow = testException

        assertFailsWithTestException {
            sut.updateModules(
                isEpicsActivated = getRandomBoolean(),
                isBacklogActivated = getRandomBoolean(),
                isKanbanActivated = getRandomBoolean(),
                isIssuesActivated = getRandomBoolean(),
                isWikiActivated = getRandomBoolean(),
                totalMilestones = null,
                totalStoryPoints = null
            )
        }
        assertTrue(projectDao.insertCalls.isEmpty())
    }

    @Test
    fun `on updateProject error propagates`() = runTest {
        projectsApi.errorToThrow = testException

        assertFailsWithTestException {
            sut.updateProject(
                name = getRandomString(),
                description = getRandomString(),
                isPrivate = getRandomBoolean(),
                isLookingForPeople = getRandomBoolean(),
                lookingForPeopleNote = getRandomString(),
                isContactActivated = getRandomBoolean()
            )
        }
        assertTrue(projectDao.insertCalls.isEmpty())
    }

    @Test
    fun `on getTagsColors error propagates`() = runTest {
        projectsApi.errorToThrow = testException

        assertFailsWithTestException { sut.getTagsColors() }
    }

    @Test
    fun `on deleteTag error propagates`() = runTest {
        projectsApi.errorToThrow = testException

        assertFailsWithTestException { sut.deleteTag(getRandomString()) }
    }

    @Test
    fun `on createTag error propagates`() = runTest {
        projectsApi.errorToThrow = testException

        assertFailsWithTestException { sut.createTag(getRandomString(), getRandomString()) }
    }

    @Test
    fun `on editTag error propagates`() = runTest {
        projectsApi.errorToThrow = testException

        assertFailsWithTestException { sut.editTag(getRandomString(), getRandomString(), getRandomString()) }
    }

    @Test
    fun `on mixTags error propagates`() = runTest {
        projectsApi.errorToThrow = testException

        assertFailsWithTestException { sut.mixTags(listOf(getRandomString()), getRandomString()) }
    }
}
