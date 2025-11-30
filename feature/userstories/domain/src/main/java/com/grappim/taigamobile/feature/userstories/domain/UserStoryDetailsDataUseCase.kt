package com.grappim.taigamobile.feature.userstories.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class UserStoryDetailsDataUseCase @Inject constructor(
    private val filtersRepository: FiltersRepository,
    private val userStoriesRepository: UserStoriesRepository,
    private val historyRepository: HistoryRepository,
    private val sprintsRepository: SprintsRepository,
    private val usersRepository: UsersRepository,
    private val workItemRepository: WorkItemRepository
) {

    suspend fun getUserStoryData(id: Long) = resultOf {
        coroutineScope {
            val taskType = CommonTaskType.UserStory
            val filtersData = async { filtersRepository.getFiltersData(taskType) }

            val userStoryDeferred = async {
                userStoriesRepository.getUserStory(id = id)
            }

            val attachments = async {
                workItemRepository.getWorkItemAttachments(
                    workItemId = id,
                    commonTaskType = taskType
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

            val userStory = userStoryDeferred.await()

            val sprint = async {
                userStory.milestone?.let { sprintsRepository.getSprint(sprintId = it) }
            }

            val creator = async { usersRepository.getUser(userStory.creatorId) }

            val assigneesDeferred =
                async { usersRepository.getUsersList(userStory.assignedUserIds) }
            val watchersDeferred = async { usersRepository.getUsersList(userStory.watcherUserIds) }

            val assignees = assigneesDeferred.await()
            val watchers = watchersDeferred.await()

            val isAssignedToMe = async { usersRepository.isAnyAssignedToMe(assignees) }
            val isWatchedByMe = async { usersRepository.isAnyAssignedToMe(watchers) }

            UserStoryDetailsData(
                userStory = userStory,
                attachments = attachments.await(),
                sprint = sprint.await(),
                customFields = customFields.await(),
                comments = commentsDeferred.await(),
                creator = creator.await(),
                assignees = assignees,
                watchers = watchers,
                isAssignedToMe = isAssignedToMe.await(),
                isWatchedByMe = isWatchedByMe.await(),
                filtersData = filtersData.await()
            )
        }
    }

    suspend fun deleteUserStory(id: Long) = resultOf {
        userStoriesRepository.deleteUserStory(id)
    }
}
