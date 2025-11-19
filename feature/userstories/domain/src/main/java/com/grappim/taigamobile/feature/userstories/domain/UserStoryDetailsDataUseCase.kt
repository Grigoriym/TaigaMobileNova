package com.grappim.taigamobile.feature.userstories.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.domain.patch.PatchedCustomAttributes
import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.AssigneesData
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WatchersData
import com.grappim.taigamobile.feature.workitem.domain.WatchersListUpdateData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class UserStoryDetailsDataUseCase @Inject constructor(
    private val filtersRepository: FiltersRepository,
    private val userStoriesRepository: UserStoriesRepository,
    private val historyRepository: HistoryRepository,
    private val sprintsRepository: SprintsRepository,
    private val usersRepository: UsersRepository,
    private val patchDataGenerator: PatchDataGenerator
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

    suspend fun patchData(
        version: Long,
        userStoryId: Long,
        payload: ImmutableMap<String, Any?>
    ): Result<PatchedData> = resultOf {
        userStoriesRepository.patchData(
            version = version,
            userStoryId = userStoryId,
            payload = payload
        )
    }

    suspend fun patchCustomAttributes(
        version: Long,
        userStoryId: Long,
        payload: ImmutableMap<String, Any?>
    ): Result<PatchedCustomAttributes> = resultOf {
        userStoriesRepository.patchCustomAttributes(
            version = version,
            userStoryId = userStoryId,
            payload = payload
        )
    }

    suspend fun updateAssigneesData(
        version: Long,
        userStoryId: Long,
        assigneesList: ImmutableList<Long>
    ) = resultOf {
        coroutineScope {
            val payload = patchDataGenerator.getAssignedUsersPatchPayload(
                assignees = assigneesList
            )
            val patchedData = userStoriesRepository.patchData(
                version = version,
                userStoryId = userStoryId,
                payload = payload
            )

            val assignees = usersRepository.getUsersList(assigneesList)
            val isAssignedToMe = usersRepository.isAnyAssignedToMe(assignees)

            AssigneesData(
                assignees = assignees,
                isAssignedToMe = isAssignedToMe,
                newVersion = patchedData.newVersion
            )
        }
    }

    suspend fun updateWatchersData(
        version: Long,
        userStoryId: Long,
        newList: ImmutableList<Long>
    ): Result<WatchersListUpdateData> = resultOf {
        coroutineScope {
            val payload = mapOf("watchers" to newList).toPersistentMap()
            val patchedData = userStoriesRepository.patchData(
                version = version,
                userStoryId = userStoryId,
                payload = payload
            )

            val watchers: ImmutableList<User>
            val isWatchedByMe: Boolean
            if (newList.isEmpty()) {
                watchers = persistentListOf()
                isWatchedByMe = false
            } else {
                watchers = usersRepository.getUsersList(newList).toPersistentList()
                isWatchedByMe = usersRepository.isAnyAssignedToMe(watchers)
            }

            WatchersListUpdateData(
                version = patchedData.newVersion,
                isWatchedByMe = isWatchedByMe,
                watchers = watchers
            )
        }
    }

    suspend fun removeMeFromWatchers(userStoryId: Long) = resultOf {
        coroutineScope {
            userStoriesRepository.unwatchUserStory(userStoryId)

            val userStory = userStoriesRepository.getUserStory(id = userStoryId)

            val watchers = usersRepository.getUsersList(userStory.watcherUserIds)

            val isWatchedByMe = usersRepository.isAnyAssignedToMe(watchers)

            WatchersData(
                watchers = watchers.toImmutableList(),
                isWatchedByMe = isWatchedByMe
            )
        }
    }

    suspend fun addMeToWatchers(userStoryId: Long) = resultOf {
        coroutineScope {
            userStoriesRepository.watchUserStory(userStoryId)

            val issue = userStoriesRepository.getUserStory(id = userStoryId)

            val watchers = usersRepository.getUsersList(issue.watcherUserIds)

            val isWatchedByMe = usersRepository.isAnyAssignedToMe(watchers)

            WatchersData(
                watchers = watchers.toImmutableList(),
                isWatchedByMe = isWatchedByMe
            )
        }
    }
}
