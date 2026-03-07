package com.grappim.taigamobile.testing.api

import com.grappim.taigamobile.feature.tasks.data.CreateTaskRequestDTO
import com.grappim.taigamobile.feature.tasks.data.TasksApi
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO

class FakeTasksApi : TasksApi {

    data class GetTasksCall(
        val userStory: Any?,
        val project: Long?,
        val sprint: Long?,
        val page: Int?,
        val assignedId: Long?,
        val isClosed: Boolean?,
        val watcherId: Long?
    )

    var getTasksResult: List<WorkItemResponseDTO> = emptyList()
    val getTasksCalls = mutableListOf<GetTasksCall>()

    var createTaskResult: WorkItemResponseDTO? = null
    val createTaskCalls = mutableListOf<CreateTaskRequestDTO>()

    override suspend fun getTasks(
        userStory: Any?,
        project: Long?,
        sprint: Long?,
        page: Int?,
        assignedId: Long?,
        isClosed: Boolean?,
        watcherId: Long?
    ): List<WorkItemResponseDTO> {
        getTasksCalls += GetTasksCall(userStory, project, sprint, page, assignedId, isClosed, watcherId)
        return getTasksResult
    }

    override suspend fun createTask(createTaskRequestDTO: CreateTaskRequestDTO): WorkItemResponseDTO {
        createTaskCalls += createTaskRequestDTO
        return createTaskResult ?: error("createTaskResult not set")
    }
}
