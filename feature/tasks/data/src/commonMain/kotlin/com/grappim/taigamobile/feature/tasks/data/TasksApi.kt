package com.grappim.taigamobile.feature.tasks.data

import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.koin.core.annotation.Single

@Single
class TasksApi(private val httpClient: HttpClient) {
    suspend fun getTasks(
        userStory: Any? = null,
        project: Long? = null,
        sprint: Long? = null,
        page: Int? = null,
        assignedId: Long? = null,
        isClosed: Boolean? = null,
        watcherId: Long? = null
    ): List<WorkItemResponseDTO> = httpClient.get("tasks") {
        url {
            parameters.append("order_by", "us_order")
            if (userStory != null) parameters.append("user_story", userStory.toString())
            if (project != null) parameters.append("project", project.toString())
            if (sprint != null) parameters.append("milestone", sprint.toString())
            if (page != null) parameters.append("page", page.toString())
            if (assignedId != null) parameters.append("assigned_to", assignedId.toString())
            if (isClosed != null) parameters.append("status__is_closed", isClosed.toString())
            if (watcherId != null) parameters.append("watchers", watcherId.toString())
        }
        if (page == null) headers.append("x-disable-pagination", "true")
    }.body()

    suspend fun createTask(createTaskRequestDTO: CreateTaskRequestDTO): WorkItemResponseDTO = httpClient.post("tasks") {
        setBody(createTaskRequestDTO)
    }.body()
}
