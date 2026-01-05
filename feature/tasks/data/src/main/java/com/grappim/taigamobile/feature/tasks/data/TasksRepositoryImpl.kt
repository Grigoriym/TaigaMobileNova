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
import kotlinx.collections.immutable.ImmutableList
import javax.inject.Inject

class TasksRepositoryImpl @Inject constructor(
    private val tasksApi: TasksApi,
    private val taigaSessionStorage: TaigaSessionStorage,
    private val workItemApi: WorkItemApi,
    private val taskMapper: TaskMapper,
    private val workItemMapper: WorkItemMapper
) : TasksRepository {
    override suspend fun getUserStoryTasks(storyId: Long): ImmutableList<Task> {
        val dtos = tasksApi.getTasks(
            userStory = storyId,
            project = taigaSessionStorage.getCurrentProjectId()
        )
        return taskMapper.toDomainList(dtos)
    }

    override suspend fun getTasks(
        assignedId: Long?,
        isClosed: Boolean?,
        watcherId: Long?,
        userStory: Any?,
        project: Long?,
        sprint: Long?
    ): ImmutableList<Task> {
        val dtos = tasksApi.getTasks(
            assignedId = assignedId,
            isClosed = isClosed,
            watcherId = watcherId,
            userStory = userStory,
            project = project,
            sprint = sprint
        )
        return taskMapper.toDomainList(dtos)
    }

    override suspend fun getTask(id: Long): Task {
        val response = workItemApi.getWorkItemById(
            taskPath = WorkItemPathPlural(CommonTaskType.Task),
            id = id
        )
        return taskMapper.toDomain(response)
    }

    override suspend fun deleteTask(id: Long) {
        workItemApi.deleteWorkItem(
            taskPath = WorkItemPathPlural(CommonTaskType.Task),
            workItemId = id
        )
    }

    override suspend fun createTask(title: String, description: String, parentId: Long?, sprintId: Long?): WorkItem {
        val response = tasksApi.createTask(
            CreateTaskRequestDTO(
                project = taigaSessionStorage.getCurrentProjectId(),
                subject = title,
                description = description,
                milestone = sprintId,
                userStory = parentId
            )
        )
        return workItemMapper.toDomain(response, CommonTaskType.Task)
    }
}
