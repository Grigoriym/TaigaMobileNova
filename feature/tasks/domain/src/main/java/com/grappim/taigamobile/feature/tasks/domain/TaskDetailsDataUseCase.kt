package com.grappim.taigamobile.feature.tasks.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.projects.domain.canCommentTask
import com.grappim.taigamobile.feature.projects.domain.canDeleteTask
import com.grappim.taigamobile.feature.projects.domain.canModifyTask
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class TaskDetailsDataUseCase @Inject constructor(
    private val filtersRepository: FiltersRepository,
    private val tasksRepository: TasksRepository,
    private val historyRepository: HistoryRepository,
    private val sprintsRepository: SprintsRepository,
    private val usersRepository: UsersRepository,
    private val workItemRepository: WorkItemRepository,
    private val projectsRepository: ProjectsRepository
) {

    suspend fun getTaskData(id: Long) = resultOf {
        coroutineScope {
            val taskType = CommonTaskType.Task
            val filtersData = async { filtersRepository.getFiltersData(taskType) }

            val taskDeferred = async {
                tasksRepository.getTask(id = id)
            }

            val attachments = async {
                workItemRepository.getWorkItemAttachments(
                    workItemId = id,
                    taskIdentifier = TaskIdentifier.WorkItem(taskType)
                )
            }
            val customFields = async {
                workItemRepository.getCustomFields(
                    workItemId = id,
                    commonTaskType = taskType
                )
            }
            val commentsDeferred = async {
                historyRepository.getComments(
                    commonTaskId = id,
                    type = taskType
                )
            }

            val task = taskDeferred.await()

            val sprint = async {
                task.milestone?.let { sprintsRepository.getSprint(sprintId = it) }
            }

            val creator = async { usersRepository.getUser(task.creatorId) }

            val assigneesDeferred =
                async { usersRepository.getUsersList(task.assignedUserIds) }
            val watchersDeferred = async { usersRepository.getUsersList(task.watcherUserIds) }

            val assignees = assigneesDeferred.await()
            val watchers = watchersDeferred.await()

            val isAssignedToMe = async { usersRepository.isAnyAssignedToMe(assignees) }
            val isWatchedByMe = async { usersRepository.isAnyAssignedToMe(watchers) }

            TaskDetailsData(
                task = task,
                attachments = attachments.await(),
                sprint = sprint.await(),
                customFields = customFields.await(),
                comments = commentsDeferred.await(),
                creator = creator.await(),
                assignees = assignees,
                watchers = watchers,
                isAssignedToMe = isAssignedToMe.await(),
                isWatchedByMe = isWatchedByMe.await(),
                filtersData = filtersData.await(),
                canComment = projectsRepository.getPermissions().canCommentTask(),
                canDeleteTask = projectsRepository.getPermissions().canDeleteTask(),
                canModifyTask = projectsRepository.getPermissions().canModifyTask()
            )
        }
    }

    suspend fun deleteTask(id: Long) = resultOf {
        tasksRepository.deleteTask(id)
    }
}
