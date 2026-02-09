package com.grappim.taigamobile.feature.sprint.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.db.dao.SprintDao
import com.grappim.taigamobile.core.storage.db.dao.WorkItemDao
import com.grappim.taigamobile.core.storage.network.NetworkMonitor
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.data.WorkItemEntityMapper
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import com.grappim.taigamobile.testing.FakeNetworkMonitor
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.getSprint
import com.grappim.taigamobile.testing.getSprintResponseDTO
import com.grappim.taigamobile.testing.getStatus
import com.grappim.taigamobile.testing.getWorkItem
import com.grappim.taigamobile.testing.getWorkItemResponseDTO
import com.grappim.taigamobile.testing.nowLocalDate
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SprintsRepositoryImplTest {

    private val sprintApi: SprintApi = mockk()
    private val taigaSessionStorage: TaigaSessionStorage = mockk()
    private val filtersRepository: FiltersRepository = mockk()
    private val workItemMapper: WorkItemMapper = mockk()
    private val workItemEntityMapper: WorkItemEntityMapper = mockk()
    private val workItemApi: WorkItemApi = mockk()
    private val sprintMapper: SprintMapper = mockk()
    private val sprintDao: SprintDao = mockk()
    private val workItemDao: WorkItemDao = mockk()
    private val networkMonitor: NetworkMonitor = FakeNetworkMonitor()

    private lateinit var sut: SprintsRepository

    private val projectId = getRandomLong()

    @Before
    fun setup() {
        coEvery { taigaSessionStorage.getCurrentProjectId() } returns projectId
        every { workItemEntityMapper.toEntityList(any(), any()) } returns emptyList()

        sut = SprintsRepositoryImpl(
            sprintApi = sprintApi,
            taigaSessionStorage = taigaSessionStorage,
            filtersRepository = filtersRepository,
            workItemMapper = workItemMapper,
            workItemEntityMapper = workItemEntityMapper,
            workItemApi = workItemApi,
            sprintMapper = sprintMapper,
            sprintDao = sprintDao,
            workItemDao = workItemDao,
            networkMonitor = networkMonitor
        )
    }

    @Test
    fun `getSprints should return mapped sprints`() = runTest {
        val dtos = listOf(getSprintResponseDTO(), getSprintResponseDTO())
        val expectedSprints = persistentListOf(getSprint(), getSprint())

        coEvery { sprintApi.getSprints(project = projectId, isClosed = false) } returns dtos
        coEvery { sprintMapper.toDomainList(dtos) } returns expectedSprints
        coJustRun { sprintDao.deleteByProjectId(projectId) }
        coJustRun { sprintDao.insertAll(any()) }

        val result = sut.getSprints(isClosed = false)

        assertEquals(expectedSprints, result)
        coVerify { sprintApi.getSprints(project = projectId, isClosed = false) }
    }

    @Test
    fun `getSprints should filter by closed status`() = runTest {
        val dtos = listOf(getSprintResponseDTO())
        val expectedSprints = persistentListOf(getSprint())

        coEvery { sprintApi.getSprints(project = projectId, isClosed = true) } returns dtos
        coEvery { sprintMapper.toDomainList(dtos) } returns expectedSprints
        coJustRun { sprintDao.deleteByProjectId(projectId) }
        coJustRun { sprintDao.insertAll(any()) }

        val result = sut.getSprints(isClosed = true)

        assertEquals(expectedSprints, result)
        coVerify { sprintApi.getSprints(project = projectId, isClosed = true) }
    }

    @Test
    fun `getSprint should return mapped sprint by id`() = runTest {
        val sprintId = getRandomLong()
        val dto = getSprintResponseDTO()
        val expectedSprint = getSprint()

        coEvery { sprintApi.getSprint(sprintId) } returns dto
        coEvery { sprintMapper.toDomain(dto) } returns expectedSprint

        val result = sut.getSprint(sprintId)

        assertEquals(expectedSprint, result)
        coVerify { sprintApi.getSprint(sprintId) }
    }

    @Test
    fun `getSprintUserStories should return mapped user stories`() = runTest {
        val sprintId = getRandomLong()
        val taskPath = WorkItemPathPlural(CommonTaskType.UserStory)
        val dtos = listOf(getWorkItemResponseDTO(), getWorkItemResponseDTO())
        val stories = persistentListOf(
            getWorkItem(taskType = CommonTaskType.UserStory),
            getWorkItem(taskType = CommonTaskType.UserStory)
        )

        coEvery {
            workItemApi.getWorkItems(
                taskPath = taskPath,
                project = projectId,
                sprint = sprintId
            )
        } returns dtos
        coEvery { workItemMapper.toDomainList(dtos, CommonTaskType.UserStory) } returns stories
        coJustRun { workItemDao.deleteByProjectIdAndType(projectId, CommonTaskType.UserStory) }
        coJustRun { workItemDao.insertAll(any()) }

        val result = sut.getSprintUserStories(sprintId)

        assertEquals(stories, result)
    }

    @Test
    fun `getSprintTasks should return storyless tasks`() = runTest {
        val sprintId = getRandomLong()
        val taskPath = WorkItemPathPlural(CommonTaskType.Task)
        val dtos = listOf(getWorkItemResponseDTO())
        val expectedTasks = persistentListOf(getWorkItem(taskType = CommonTaskType.Task))

        coEvery {
            workItemApi.getWorkItems(
                taskPath = taskPath,
                project = projectId,
                sprint = sprintId,
                userStory = "null"
            )
        } returns dtos
        coEvery { workItemMapper.toDomainList(dtos, CommonTaskType.Task) } returns expectedTasks
        coJustRun { workItemDao.insertAll(any()) }

        val result = sut.getSprintTasks(sprintId)

        assertEquals(expectedTasks, result)
        coVerify {
            workItemApi.getWorkItems(
                taskPath = taskPath,
                project = projectId,
                sprint = sprintId,
                userStory = "null"
            )
        }
    }

    @Test
    fun `getSprintIssues should return mapped issues`() = runTest {
        val sprintId = getRandomLong()
        val taskPath = WorkItemPathPlural(CommonTaskType.Issue)
        val dtos = listOf(getWorkItemResponseDTO())
        val expectedIssues = persistentListOf(getWorkItem(taskType = CommonTaskType.Issue))

        coEvery {
            workItemApi.getWorkItems(
                taskPath = taskPath,
                project = projectId,
                sprint = sprintId
            )
        } returns dtos
        coEvery { workItemMapper.toDomainList(dtos, CommonTaskType.Issue) } returns expectedIssues
        coJustRun { workItemDao.insertAll(any()) }

        val result = sut.getSprintIssues(sprintId)

        assertEquals(expectedIssues, result)
    }

    @Test
    fun `createSprint should call api with correct parameters`() = runTest {
        val name = getRandomString()
        val start = nowLocalDate
        val end = nowLocalDate.plusDays(14)

        val expectedRequest = CreateSprintRequest(
            name = name,
            estimatedStart = start,
            estimatedFinish = end,
            project = projectId
        )

        coJustRun { sprintApi.createSprint(expectedRequest) }

        sut.createSprint(name, start, end)

        coVerify { sprintApi.createSprint(expectedRequest) }
    }

    @Test
    fun `editSprint should call api with correct parameters`() = runTest {
        val sprintId = getRandomLong()
        val name = getRandomString()
        val start = nowLocalDate
        val end = nowLocalDate.plusDays(14)

        val expectedRequest = EditSprintRequest(name, start, end)

        coJustRun { sprintApi.editSprint(id = sprintId, request = expectedRequest) }

        sut.editSprint(sprintId, name, start, end)

        coVerify { sprintApi.editSprint(id = sprintId, request = expectedRequest) }
    }

    @Test
    fun `deleteSprint should call api with correct id`() = runTest {
        val sprintId = getRandomLong()

        coEvery { sprintApi.deleteSprint(sprintId) } returns mockk()

        sut.deleteSprint(sprintId)

        coVerify { sprintApi.deleteSprint(sprintId) }
    }

    @Test
    fun `getSprintData should return complete sprint data`() = runTest {
        val sprintId = getRandomLong()
        val sprintDto = getSprintResponseDTO()
        val expectedSprint = getSprint()
        val statuses = persistentListOf(getStatus())
        val storyDtos = listOf(getWorkItemResponseDTO())
        val stories = persistentListOf(getWorkItem(taskType = CommonTaskType.UserStory))
        val taskDtos = listOf(getWorkItemResponseDTO())
        val tasks = persistentListOf(getWorkItem(taskType = CommonTaskType.Task))
        val issueDtos = listOf(getWorkItemResponseDTO())
        val issues = persistentListOf(getWorkItem(taskType = CommonTaskType.Issue))
        val storylessTaskDtos = listOf(getWorkItemResponseDTO())
        val storylessTasks = persistentListOf(getWorkItem(taskType = CommonTaskType.Task))

        coEvery { sprintApi.getSprint(sprintId) } returns sprintDto
        coEvery { sprintMapper.toDomain(sprintDto) } returns expectedSprint
        coEvery { filtersRepository.getStatuses(CommonTaskType.Task) } returns statuses
        coJustRun { workItemDao.deleteByProjectIdAndType(any(), any()) }
        coJustRun { workItemDao.insertAll(any()) }

        coEvery {
            workItemApi.getWorkItems(
                taskPath = WorkItemPathPlural(CommonTaskType.UserStory),
                project = projectId,
                sprint = sprintId
            )
        } returns storyDtos
        coEvery { workItemMapper.toDomainList(storyDtos, CommonTaskType.UserStory) } returns stories

        stories.forEach { story ->
            coEvery {
                workItemApi.getWorkItems(
                    taskPath = WorkItemPathPlural(CommonTaskType.Task),
                    project = projectId,
                    userStory = story.id
                )
            } returns taskDtos
            coEvery { workItemMapper.toDomainList(taskDtos, CommonTaskType.Task) } returns tasks
        }

        coEvery {
            workItemApi.getWorkItems(
                taskPath = WorkItemPathPlural(CommonTaskType.Issue),
                project = projectId,
                sprint = sprintId
            )
        } returns issueDtos
        coEvery { workItemMapper.toDomainList(issueDtos, CommonTaskType.Issue) } returns issues

        coEvery {
            workItemApi.getWorkItems(
                taskPath = WorkItemPathPlural(CommonTaskType.Task),
                project = projectId,
                sprint = sprintId,
                userStory = "null"
            )
        } returns storylessTaskDtos
        coEvery { workItemMapper.toDomainList(storylessTaskDtos, CommonTaskType.Task) } returns storylessTasks

        val result = sut.getSprintData(sprintId)

        assertTrue(result.isSuccess)
        val sprintData = result.getOrThrow()
        assertEquals(expectedSprint, sprintData.sprint)
        assertEquals(statuses, sprintData.statuses)
        assertEquals(issues, sprintData.issues)
        assertEquals(storylessTasks, sprintData.storylessTasks)
    }
}
