package com.grappim.taigamobile.feature.tasks.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.tasks.domain.Task
import com.grappim.taigamobile.feature.tasks.domain.TasksRepository
import com.grappim.taigamobile.feature.tasks.mapper.TaskMapper
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.getWorkItemResponseDTO
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TasksRepositoryImplTest {

    private val tasksApi: TasksApi = mockk()
    private val taigaSessionStorage: TaigaSessionStorage = mockk()
    private val workItemApi: WorkItemApi = mockk()
    private val taskMapper: TaskMapper = mockk()
    private val workItemMapper: WorkItemMapper = mockk()

    private lateinit var sut: TasksRepository

    private val projectId = getRandomLong()
    private val taskPath = WorkItemPathPlural(CommonTaskType.Task)

    @Before
    fun setup() {
        coEvery { taigaSessionStorage.getCurrentProjectId() } returns projectId

        sut = TasksRepositoryImpl(
            tasksApi = tasksApi,
            taigaSessionStorage = taigaSessionStorage,
            workItemApi = workItemApi,
            taskMapper = taskMapper,
            workItemMapper = workItemMapper
        )
    }

    @Test
    fun `getUserStoryTasks should return mapped tasks for user story`() = runTest {
        val storyId = getRandomLong()
        val dtos = listOf(getWorkItemResponseDTO(), getWorkItemResponseDTO())
        val expectedTasks = persistentListOf(mockk<Task>(), mockk<Task>())

        coEvery { tasksApi.getTasks(userStory = storyId, project = projectId) } returns dtos
        every { taskMapper.toDomainList(dtos) } returns expectedTasks

        val result = sut.getUserStoryTasks(storyId)

        assertEquals(expectedTasks, result)
        coVerify { tasksApi.getTasks(userStory = storyId, project = projectId) }
        verify { taskMapper.toDomainList(dtos) }
    }

    @Test
    fun `getUserStoryTasks should return empty list when no tasks`() = runTest {
        val storyId = getRandomLong()
        val dtos = emptyList<com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO>()
        val expectedTasks = persistentListOf<Task>()

        coEvery { tasksApi.getTasks(userStory = storyId, project = projectId) } returns dtos
        every { taskMapper.toDomainList(dtos) } returns expectedTasks

        val result = sut.getUserStoryTasks(storyId)

        assertEquals(0, result.size)
    }

    @Test
    fun `getTasks should return mapped tasks with all filters`() = runTest {
        val assignedId = getRandomLong()
        val watcherId = getRandomLong()
        val sprintId = getRandomLong()
        val customProjectId = getRandomLong()
        val dtos = listOf(getWorkItemResponseDTO())
        val expectedTasks = persistentListOf(mockk<Task>())

        coEvery {
            tasksApi.getTasks(
                assignedId = assignedId,
                isClosed = true,
                watcherId = watcherId,
                userStory = null,
                project = customProjectId,
                sprint = sprintId
            )
        } returns dtos
        every { taskMapper.toDomainList(dtos) } returns expectedTasks

        val result = sut.getTasks(
            assignedId = assignedId,
            isClosed = true,
            watcherId = watcherId,
            userStory = null,
            project = customProjectId,
            sprint = sprintId
        )

        assertEquals(expectedTasks, result)
    }

    @Test
    fun `getTasks should handle null filters`() = runTest {
        val dtos = listOf(getWorkItemResponseDTO())
        val expectedTasks = persistentListOf(mockk<Task>())

        coEvery {
            tasksApi.getTasks(
                assignedId = null,
                isClosed = null,
                watcherId = null,
                userStory = null,
                project = null,
                sprint = null
            )
        } returns dtos
        every { taskMapper.toDomainList(dtos) } returns expectedTasks

        val result = sut.getTasks()

        assertEquals(expectedTasks, result)
    }

    @Test
    fun `getTask should return mapped task by id`() = runTest {
        val taskId = getRandomLong()
        val dto = getWorkItemResponseDTO()
        val expectedTask = mockk<Task>()

        coEvery { workItemApi.getWorkItemById(taskPath = taskPath, id = taskId) } returns dto
        every { taskMapper.toDomain(dto) } returns expectedTask

        val result = sut.getTask(taskId)

        assertEquals(expectedTask, result)
        coVerify { workItemApi.getWorkItemById(taskPath = taskPath, id = taskId) }
        verify { taskMapper.toDomain(dto) }
    }

    @Test
    fun `deleteTask should call workItemApi with correct parameters`() = runTest {
        val taskId = getRandomLong()

        coJustRun { workItemApi.deleteWorkItem(taskPath = taskPath, workItemId = taskId) }

        sut.deleteTask(taskId)

        coVerify { workItemApi.deleteWorkItem(taskPath = taskPath, workItemId = taskId) }
    }

    @Test
    fun `createTask should return mapped work item`() = runTest {
        val title = getRandomString()
        val description = getRandomString()
        val parentId = getRandomLong()
        val sprintId = getRandomLong()
        val dto = getWorkItemResponseDTO()
        val expectedWorkItem = mockk<WorkItem>()

        val expectedRequest = CreateTaskRequestDTO(
            project = projectId,
            subject = title,
            description = description,
            milestone = sprintId,
            userStory = parentId
        )

        coEvery { tasksApi.createTask(expectedRequest) } returns dto
        every { workItemMapper.toDomain(dto, CommonTaskType.Task) } returns expectedWorkItem

        val result = sut.createTask(title, description, parentId, sprintId)

        assertEquals(expectedWorkItem, result)
        coVerify { tasksApi.createTask(expectedRequest) }
        verify { workItemMapper.toDomain(dto, CommonTaskType.Task) }
    }

    @Test
    fun `createTask should handle null parentId and sprintId`() = runTest {
        val title = getRandomString()
        val description = getRandomString()
        val dto = getWorkItemResponseDTO()
        val expectedWorkItem = mockk<WorkItem>()

        val expectedRequest = CreateTaskRequestDTO(
            project = projectId,
            subject = title,
            description = description,
            milestone = null,
            userStory = null
        )

        coEvery { tasksApi.createTask(expectedRequest) } returns dto
        every { workItemMapper.toDomain(dto, CommonTaskType.Task) } returns expectedWorkItem

        val result = sut.createTask(title, description, null, null)

        assertEquals(expectedWorkItem, result)
        coVerify { tasksApi.createTask(expectedRequest) }
    }
}
