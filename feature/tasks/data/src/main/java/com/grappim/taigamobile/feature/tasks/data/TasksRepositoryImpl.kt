package com.grappim.taigamobile.feature.tasks.data

import com.grappim.taigamobile.core.api.CommonTaskMapper
import com.grappim.taigamobile.core.api.handle404
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.tasks.domain.TasksRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class TasksRepositoryImpl @Inject constructor(
    private val tasksApi: TasksApi,
    private val taigaStorage: TaigaStorage,
    private val commonTaskMapper: CommonTaskMapper
) : TasksRepository {
    override suspend fun getUserStoryTasks(storyId: Long) = handle404 {
        tasksApi.getTasks(userStory = storyId, project = taigaStorage.currentProjectIdFlow.first())
            .map { commonTaskMapper.toDomain(resp = it, commonTaskType = CommonTaskType.Task) }
    }

    override suspend fun getTasks(
        assignedId: Long?,
        isClosed: Boolean?,
        watcherId: Long?,
        userStory: Any?,
        project: Long?,
        sprint: Long?
    ): List<CommonTask> = tasksApi.getTasks(
        assignedId = assignedId,
        isClosed = isClosed,
        watcherId = watcherId,
        userStory = userStory,
        project = project,
        sprint = sprint
    ).map { commonTaskMapper.toDomain(resp = it, commonTaskType = CommonTaskType.Task) }
}
