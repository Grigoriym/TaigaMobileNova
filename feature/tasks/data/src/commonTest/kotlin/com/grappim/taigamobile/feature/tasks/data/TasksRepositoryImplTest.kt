package com.grappim.taigamobile.feature.tasks.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.feature.tasks.domain.TasksRepository
import com.grappim.taigamobile.feature.tasks.mapper.TaskMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.workitem.domain.getPluralPath
import com.grappim.taigamobile.feature.workitem.mapper.DueDateStatusMapper
import com.grappim.taigamobile.feature.workitem.mapper.UserStoryShortInfoMapper
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import com.grappim.taigamobile.testing.api.FakeTasksApi
import com.grappim.taigamobile.testing.api.FakeWorkItemApi
import com.grappim.taigamobile.testing.models.getWorkItemResponseDTO
import com.grappim.taigamobile.testing.storage.FakeServerStorage
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TasksRepositoryImplTest {

    private val projectId = getRandomLong()

    private val tasksApi = FakeTasksApi()
    private val taigaSessionStorage = FakeTaigaSessionStorage(currentProjectId = projectId)
    private val workItemApi = FakeWorkItemApi()
    private val taskMapper = TaskMapper(
        serverStorage = FakeServerStorage(),
        userMapper = UserMapper(),
        statusesMapper = StatusesMapper(),
        projectMapper = ProjectMapper(),
        tagsMapper = TagsMapper(),
        dueDateStatusMapper = DueDateStatusMapper(),
        userStoryShortInfoMapper = UserStoryShortInfoMapper()
    )
    private val workItemMapper = WorkItemMapper(
        statusesMapper = StatusesMapper(),
        userMapper = UserMapper(),
        tagsMapper = TagsMapper(),
        projectMapper = ProjectMapper()
    )

    private val sut: TasksRepository = TasksRepositoryImpl(
        tasksApi = tasksApi,
        taigaSessionStorage = taigaSessionStorage,
        workItemApi = workItemApi,
        taskMapper = taskMapper,
        workItemMapper = workItemMapper
    )

    @Test
    fun `getUserStoryTasks should return mapped tasks for user story`() = runTest {
        val storyId = getRandomLong()
        val dtos = listOf(getWorkItemResponseDTO(), getWorkItemResponseDTO())
        tasksApi.getTasksResult = dtos

        val result = sut.getUserStoryTasks(storyId)

        assertEquals(taskMapper.toDomainList(dtos), result)
        val call = tasksApi.getTasksCalls.single()
        assertEquals(storyId, call.userStory)
        assertEquals(projectId, call.project)
    }

    @Test
    fun `getUserStoryTasks should return empty list when no tasks`() = runTest {
        val storyId = getRandomLong()
        tasksApi.getTasksResult = emptyList()

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
        tasksApi.getTasksResult = dtos

        val result = sut.getTasks(
            assignedId = assignedId,
            isClosed = true,
            watcherId = watcherId,
            userStory = null,
            project = customProjectId,
            sprint = sprintId
        )

        assertEquals(taskMapper.toDomainList(dtos), result)
        val call = tasksApi.getTasksCalls.single()
        assertEquals(assignedId, call.assignedId)
        assertEquals(true, call.isClosed)
        assertEquals(watcherId, call.watcherId)
        assertEquals(null, call.userStory)
        assertEquals(customProjectId, call.project)
        assertEquals(sprintId, call.sprint)
    }

    @Test
    fun `getTasks should handle null filters`() = runTest {
        val dtos = listOf(getWorkItemResponseDTO())
        tasksApi.getTasksResult = dtos

        val result = sut.getTasks()

        assertEquals(taskMapper.toDomainList(dtos), result)
        val call = tasksApi.getTasksCalls.single()
        assertEquals(null, call.assignedId)
        assertEquals(null, call.isClosed)
        assertEquals(null, call.watcherId)
        assertEquals(null, call.userStory)
        assertEquals(null, call.project)
        assertEquals(null, call.sprint)
    }

    @Test
    fun `getTask should return mapped task by id`() = runTest {
        val taskId = getRandomLong()
        val dto = getWorkItemResponseDTO()
        workItemApi.workItemByIdResponse = dto

        val result = sut.getTask(taskId)

        assertEquals(taskMapper.toDomain(dto), result)
    }

    @Test
    fun `deleteTask should call workItemApi with correct parameters`() = runTest {
        val taskId = getRandomLong()

        sut.deleteTask(taskId)

        val call = workItemApi.deleteWorkItemCalls.single()
        assertEquals(CommonTaskType.Task.getPluralPath(), call.first)
        assertEquals(taskId, call.second)
    }

    @Test
    fun `createTask should return mapped work item`() = runTest {
        val title = getRandomString()
        val description = getRandomString()
        val parentId = getRandomLong()
        val sprintId = getRandomLong()
        val dto = getWorkItemResponseDTO()
        tasksApi.createTaskResult = dto

        val result = sut.createTask(title, description, parentId, sprintId)

        assertEquals(workItemMapper.toDomain(dto, CommonTaskType.Task), result)
        val call = tasksApi.createTaskCalls.single()
        assertEquals(projectId, call.project)
        assertEquals(title, call.subject)
        assertEquals(description, call.description)
        assertEquals(sprintId, call.milestone)
        assertEquals(parentId, call.userStory)
    }

    @Test
    fun `createTask should handle null parentId and sprintId`() = runTest {
        val title = getRandomString()
        val description = getRandomString()
        val dto = getWorkItemResponseDTO()
        tasksApi.createTaskResult = dto

        val result = sut.createTask(title, description, null, null)

        assertEquals(workItemMapper.toDomain(dto, CommonTaskType.Task), result)
        val call = tasksApi.createTaskCalls.single()
        assertEquals(null, call.milestone)
        assertEquals(null, call.userStory)
    }
}
