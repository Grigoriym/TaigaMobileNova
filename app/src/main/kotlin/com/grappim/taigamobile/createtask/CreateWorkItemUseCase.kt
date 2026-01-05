package com.grappim.taigamobile.createtask

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import com.grappim.taigamobile.feature.tasks.domain.TasksRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import javax.inject.Inject

class CreateWorkItemUseCase @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val issuesRepository: IssuesRepository,
    private val userStoriesRepository: UserStoriesRepository,
    private val workItemRepository: WorkItemRepository
) {

    suspend fun createTask(
        commonTaskType: CommonTaskType,
        title: String,
        description: String,
        parentId: Long?,
        sprintId: Long?,
        statusId: Long?,
        swimlaneId: Long?
    ): Result<CreateWorkItemData> = resultOf {
        val result = when (commonTaskType) {
            CommonTaskType.Task -> tasksRepository.createTask(
                title = title,
                description = description,
                sprintId = sprintId,
                parentId = parentId
            )

            CommonTaskType.Issue -> issuesRepository.createIssue(
                title = title,
                description = description,
                sprintId = sprintId
            )

            CommonTaskType.UserStory -> userStoriesRepository.createUserStory(
                subject = title,
                description = description,
                status = statusId,
                swimlane = swimlaneId
            )

            else -> workItemRepository.createWorkItem(
                commonTaskType = commonTaskType,
                subject = title,
                description = description,
                status = statusId
            )
        }

        CreateWorkItemData(
            id = result.id,
            type = commonTaskType,
            ref = result.ref
        )
    }
}
