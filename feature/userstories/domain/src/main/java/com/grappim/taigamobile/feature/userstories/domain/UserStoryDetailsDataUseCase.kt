package com.grappim.taigamobile.feature.userstories.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class UserStoryDetailsDataUseCase @Inject constructor(
    private val filtersRepository: FiltersRepository,
    private val userStoriesRepository: UserStoriesRepository,
    private val historyRepository: HistoryRepository,
    private val sprintsRepository: SprintsRepository,
    private val usersRepository: UsersRepository
) {

    suspend fun getUserStoryData(id: Long) = resultOf {
        coroutineScope {
            val taskType = CommonTaskType.UserStory
            val filtersData = async { filtersRepository.getFiltersData(taskType) }

            val userStoryDeferred = async {
                userStoriesRepository.getUserStory(id = id)
            }

            val attachments = async { userStoriesRepository.getUserStoryAttachments(taskId = id) }
            val customFields = async {
                userStoriesRepository.getCustomFields(id = id)
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
                attachments = attachments.await().toImmutableList(),
                sprint = sprint.await(),
                customFields = customFields.await(),
                comments = commentsDeferred.await(),
                creator = creator.await(),
                assignees = assignees.toImmutableList(),
                watchers = watchers.toImmutableList(),
                isAssignedToMe = isAssignedToMe.await(),
                isWatchedByMe = isWatchedByMe.await(),
                filtersData = filtersData.await()
            )
        }
    }

    suspend fun deleteIssue(id: Long) = resultOf {
        userStoriesRepository.deleteIssue(id)
    }

    suspend fun patchData(version: Long, userStoryId: Long, payload: ImmutableMap<String, Any?>): Result<PatchedData> =
        resultOf {
            userStoriesRepository.patchData(
                version = version,
                userStoryId = userStoryId,
                payload = payload
            )
        }
}
