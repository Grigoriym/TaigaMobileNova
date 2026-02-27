package com.grappim.taigamobile.feature.sprint.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.workitem.data.WorkItemEntityMapper
import com.grappim.taigamobile.feature.workitem.domain.getPluralPath
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import com.grappim.taigamobile.testing.FakeNetworkMonitor
import com.grappim.taigamobile.testing.api.FakeSprintsApi
import com.grappim.taigamobile.testing.api.FakeWorkItemApi
import com.grappim.taigamobile.testing.dao.FakeSprintDao
import com.grappim.taigamobile.testing.dao.FakeWorkItemDao
import com.grappim.taigamobile.testing.models.getSprintEntity
import com.grappim.taigamobile.testing.models.getSprintResponseDTO
import com.grappim.taigamobile.testing.models.getStatus
import com.grappim.taigamobile.testing.models.getWorkItemEntity
import com.grappim.taigamobile.testing.models.getWorkItemResponseDTO
import com.grappim.taigamobile.testing.repo.FakeFiltersRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.nowLocalDate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SprintsRepositoryImplTest {

    private val fakeSprintsApi = FakeSprintsApi()
    private val fakeTaigaSessionStorage = FakeTaigaSessionStorage()
    private val fakeFiltersRepository = FakeFiltersRepository()
    private val fakeWorkItemApi = FakeWorkItemApi()
    private val fakeSprintDao = FakeSprintDao()
    private val fakeWorkItemDao = FakeWorkItemDao()
    private val networkMonitor = FakeNetworkMonitor()
    private val sprintMapper = SprintMapper()
    private val workItemMapper = WorkItemMapper(
        statusesMapper = StatusesMapper(),
        userMapper = UserMapper(),
        tagsMapper = TagsMapper(),
        projectMapper = ProjectMapper()
    )
    private val workItemEntityMapper = WorkItemEntityMapper(Json)

    private lateinit var sut: SprintsRepository

    private val projectId = getRandomLong()

    @BeforeTest
    fun setup() {
        fakeTaigaSessionStorage.currentProjectId = projectId

        sut = SprintsRepositoryImpl(
            sprintApi = fakeSprintsApi,
            taigaSessionStorage = fakeTaigaSessionStorage,
            filtersRepository = fakeFiltersRepository,
            workItemMapper = workItemMapper,
            workItemEntityMapper = workItemEntityMapper,
            workItemApi = fakeWorkItemApi,
            sprintMapper = sprintMapper,
            sprintDao = fakeSprintDao,
            workItemDao = fakeWorkItemDao,
            networkMonitor = networkMonitor
        )
    }

    @Test
    fun `getSprints should return mapped sprints`() = runTest {
        val dtos = listOf(getSprintResponseDTO(), getSprintResponseDTO())
        fakeSprintsApi.sprintsResult = dtos

        val result = sut.getSprints(isClosed = false)

        assertEquals(sprintMapper.toDomainList(dtos), result)
    }

    @Test
    fun `getSprints should filter by closed status`() = runTest {
        val dtos = listOf(getSprintResponseDTO())
        fakeSprintsApi.sprintsResult = dtos

        val result = sut.getSprints(isClosed = true)

        assertEquals(sprintMapper.toDomainList(dtos), result)
    }

    @Test
    fun `getSprint should return mapped sprint by id`() = runTest {
        val sprintId = getRandomLong()
        val dto = getSprintResponseDTO()
        fakeSprintsApi.sprintResult = dto

        val result = sut.getSprint(sprintId)

        assertEquals(sprintMapper.toDomain(dto), result)
    }

    @Test
    fun `getSprintUserStories should return mapped user stories`() = runTest {
        val sprintId = getRandomLong()
        val dtos = listOf(getWorkItemResponseDTO(), getWorkItemResponseDTO())
        fakeWorkItemApi.workItemsResponse = dtos

        val result = sut.getSprintUserStories(sprintId)

        assertEquals(workItemMapper.toDomainList(dtos, CommonTaskType.UserStory), result)
    }

    @Test
    fun `getSprintTasks should return storyless tasks`() = runTest {
        val sprintId = getRandomLong()
        val dtos = listOf(getWorkItemResponseDTO())
        fakeWorkItemApi.workItemsResponse = dtos

        val result = sut.getSprintTasks(sprintId)

        assertEquals(workItemMapper.toDomainList(dtos, CommonTaskType.Task), result)
    }

    @Test
    fun `getSprintIssues should return mapped issues`() = runTest {
        val sprintId = getRandomLong()
        val dtos = listOf(getWorkItemResponseDTO())
        fakeWorkItemApi.workItemsResponse = dtos

        val result = sut.getSprintIssues(sprintId)

        assertEquals(workItemMapper.toDomainList(dtos, CommonTaskType.Issue), result)
    }

    @Test
    fun `createSprint should call api with correct parameters`() = runTest {
        val name = getRandomString()
        val start = nowLocalDate
        val end = nowLocalDate.plus(14, DateTimeUnit.DAY)

        sut.createSprint(name, start, end)

        assertEquals(
            CreateSprintRequest(
                name = name,
                estimatedStart = start,
                estimatedFinish = end,
                project = projectId
            ),
            fakeSprintsApi.lastCreateRequest
        )
    }

    @Test
    fun `editSprint should call api with correct parameters`() = runTest {
        val sprintId = getRandomLong()
        val name = getRandomString()
        val start = nowLocalDate
        val end = nowLocalDate.plus(14, DateTimeUnit.DAY)

        sut.editSprint(sprintId, name, start, end)

        assertEquals(sprintId, fakeSprintsApi.lastEditId)
        assertEquals(EditSprintRequest(name, start, end), fakeSprintsApi.lastEditRequest)
    }

    @Test
    fun `deleteSprint should call api with correct id`() = runTest {
        val sprintId = getRandomLong()

        sut.deleteSprint(sprintId)

        assertEquals(sprintId, fakeSprintsApi.lastDeleteId)
    }

    @Test
    fun `getSprints offline should return cached sprints`() = runTest {
        val entities = listOf(
            getSprintEntity(isClosed = false, projectId = projectId),
            getSprintEntity(isClosed = false, projectId = projectId),
            getSprintEntity(isClosed = true, projectId = projectId)
        )
        fakeSprintDao.sprintsByProjectId = entities
        networkMonitor.setOnline(false)

        val result = sut.getSprints(isClosed = false)

        val expected = entities.filter { !it.isClosed }.map { it.toDomain() }
        assertEquals(expected, result)
    }

    @Test
    fun `getSprints when api throws should return cached sprints`() = runTest {
        fakeSprintsApi.shouldThrowOnGetSprints = true
        val entities = listOf(
            getSprintEntity(isClosed = false, projectId = projectId)
        )
        fakeSprintDao.sprintsByProjectId = entities

        val result = sut.getSprints(isClosed = false)

        assertEquals(entities.map { it.toDomain() }, result)
    }

    @Test
    fun `getSprintUserStories offline should return cached user stories`() = runTest {
        val sprintId = getRandomLong()
        val entities = listOf(
            getWorkItemEntity(taskType = CommonTaskType.UserStory, projectId = projectId),
            getWorkItemEntity(taskType = CommonTaskType.Task, projectId = projectId)
        )
        fakeWorkItemDao.workItemsByProjectIdAndSprint = entities
        networkMonitor.setOnline(false)

        val result = sut.getSprintUserStories(sprintId)

        val expected = workItemEntityMapper.toDomainList(entities.filter { it.taskType == CommonTaskType.UserStory })
        assertEquals(expected, result)
    }

    @Test
    fun `getSprintUserStories when api throws should return cached user stories`() = runTest {
        val sprintId = getRandomLong()
        val entities = listOf(
            getWorkItemEntity(taskType = CommonTaskType.UserStory, projectId = projectId)
        )
        fakeWorkItemDao.workItemsByProjectIdAndSprint = entities
        fakeWorkItemApi.getWorkItemsLambda = { _, _, _ -> throw RuntimeException("Network error") }

        val result = sut.getSprintUserStories(sprintId)

        assertEquals(workItemEntityMapper.toDomainList(entities), result)
    }

    @Test
    fun `getSprintTasks offline should return cached tasks`() = runTest {
        val sprintId = getRandomLong()
        val entities = listOf(
            getWorkItemEntity(taskType = CommonTaskType.Task, projectId = projectId),
            getWorkItemEntity(taskType = CommonTaskType.UserStory, projectId = projectId)
        )
        fakeWorkItemDao.workItemsByProjectIdAndSprint = entities
        networkMonitor.setOnline(false)

        val result = sut.getSprintTasks(sprintId)

        val expected = workItemEntityMapper.toDomainList(entities.filter { it.taskType == CommonTaskType.Task })
        assertEquals(expected, result)
    }

    @Test
    fun `getSprintTasks when api throws should return cached tasks`() = runTest {
        val sprintId = getRandomLong()
        val entities = listOf(
            getWorkItemEntity(taskType = CommonTaskType.Task, projectId = projectId)
        )
        fakeWorkItemDao.workItemsByProjectIdAndSprint = entities
        fakeWorkItemApi.getWorkItemsLambda = { _, _, _ -> throw RuntimeException("Network error") }

        val result = sut.getSprintTasks(sprintId)

        assertEquals(workItemEntityMapper.toDomainList(entities), result)
    }

    @Test
    fun `getSprintIssues offline should return cached issues`() = runTest {
        val sprintId = getRandomLong()
        val entities = listOf(
            getWorkItemEntity(taskType = CommonTaskType.Issue, projectId = projectId),
            getWorkItemEntity(taskType = CommonTaskType.Task, projectId = projectId)
        )
        fakeWorkItemDao.workItemsByProjectIdAndSprint = entities
        networkMonitor.setOnline(false)

        val result = sut.getSprintIssues(sprintId)

        val expected = workItemEntityMapper.toDomainList(entities.filter { it.taskType == CommonTaskType.Issue })
        assertEquals(expected, result)
    }

    @Test
    fun `getSprintIssues when api throws should return cached issues`() = runTest {
        val sprintId = getRandomLong()
        val entities = listOf(
            getWorkItemEntity(taskType = CommonTaskType.Issue, projectId = projectId)
        )
        fakeWorkItemDao.workItemsByProjectIdAndSprint = entities
        fakeWorkItemApi.getWorkItemsLambda = { _, _, _ -> throw RuntimeException("Network error") }

        val result = sut.getSprintIssues(sprintId)

        assertEquals(workItemEntityMapper.toDomainList(entities), result)
    }

    @Test
    fun `getSprintData should return complete sprint data`() = runTest {
        val sprintId = getRandomLong()
        val sprintDto = getSprintResponseDTO()
        val statuses = persistentListOf(getStatus())
        val storyDtos = listOf(getWorkItemResponseDTO())
        val taskDtos = listOf(getWorkItemResponseDTO())
        val issueDtos = listOf(getWorkItemResponseDTO())
        val storylessTaskDtos = listOf(getWorkItemResponseDTO())

        fakeSprintsApi.sprintResult = sprintDto
        fakeFiltersRepository.statusesResult = statuses
        fakeWorkItemApi.getWorkItemsLambda = { taskPath, _, userStory ->
            when {
                taskPath == CommonTaskType.UserStory.getPluralPath() -> storyDtos
                taskPath == CommonTaskType.Issue.getPluralPath() -> issueDtos
                taskPath == CommonTaskType.Task.getPluralPath() && userStory == "null" -> storylessTaskDtos
                taskPath == CommonTaskType.Task.getPluralPath() -> taskDtos
                else -> emptyList()
            }
        }

        val result = sut.getSprintData(sprintId)

        assertTrue(result.isSuccess)
        val sprintData = result.getOrThrow()
        assertEquals(sprintMapper.toDomain(sprintDto), sprintData.sprint)
        assertEquals(statuses, sprintData.statuses)
        assertEquals(workItemMapper.toDomainList(issueDtos, CommonTaskType.Issue), sprintData.issues)
        assertEquals(workItemMapper.toDomainList(storylessTaskDtos, CommonTaskType.Task), sprintData.storylessTasks)
    }
}
