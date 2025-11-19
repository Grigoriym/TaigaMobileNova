package com.grappim.taigamobile.feature.issues.domain

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
import com.grappim.taigamobile.feature.workitem.domain.CreatedCommentData
import com.grappim.taigamobile.feature.workitem.domain.WatchersData
import com.grappim.taigamobile.feature.workitem.domain.WatchersListUpdateData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class IssueDetailsDataUseCase @Inject constructor(
    private val issuesRepository: IssuesRepository,
    private val sprintsRepository: SprintsRepository,
    private val historyRepository: HistoryRepository,
    private val usersRepository: UsersRepository,
    private val filtersRepository: FiltersRepository
) {

    suspend fun removeMeFromWatchers(issueId: Long, ref: Int) = resultOf {
        coroutineScope {
            issuesRepository.unwatchIssue(issueId)

            val filtersData = filtersRepository.getFiltersData(CommonTaskType.Issue)
            val issue = issuesRepository.getIssue(id = issueId, filtersData = filtersData)

            val watchers = usersRepository.getUsersList(issue.watcherUserIds)

            val isWatchedByMe = usersRepository.isAnyAssignedToMe(watchers)

            WatchersData(
                watchers = watchers.toImmutableList(),
                isWatchedByMe = isWatchedByMe
            )
        }
    }

    suspend fun addMeToWatchers(issueId: Long, ref: Int) = resultOf {
        coroutineScope {
            issuesRepository.watchIssue(issueId)

            val filtersData = filtersRepository.getFiltersData(CommonTaskType.Issue)
            val issue = issuesRepository.getIssue(id = issueId, filtersData = filtersData)

            val watchers = usersRepository.getUsersList(issue.watcherUserIds)

            val isWatchedByMe = usersRepository.isAnyAssignedToMe(watchers)

            WatchersData(
                watchers = watchers.toImmutableList(),
                isWatchedByMe = isWatchedByMe
            )
        }
    }

    suspend fun patchCustomAttributes(
        version: Long,
        issueId: Long,
        payload: ImmutableMap<String, Any?>
    ): Result<PatchedCustomAttributes> = resultOf {
        issuesRepository.patchCustomAttributes(
            version = version,
            issueId = issueId,
            payload = payload
        )
    }

    suspend fun deleteIssue(id: Long) = resultOf {
        issuesRepository.deleteIssue(id)
    }

    suspend fun patchData(
        version: Long,
        issueId: Long,
        payload: ImmutableMap<String, Any?>
    ): Result<PatchedData> = resultOf {
        issuesRepository.patchData(version = version, issueId = issueId, payload = payload)
    }

    suspend fun updateAssigneesData(version: Long, issueId: Long, userId: Long?) = resultOf {
        coroutineScope {
            val payload = persistentMapOf("assigned_to" to userId)
            val patchedData = issuesRepository.patchData(
                version = version,
                issueId = issueId,
                payload = payload
            )

            val assignees: ImmutableList<User>
            val isAssignedToMe: Boolean
            if (userId == null) {
                assignees = persistentListOf()
                isAssignedToMe = false
            } else {
                assignees = usersRepository.getUsersList(listOf(userId)).toPersistentList()
                isAssignedToMe = usersRepository.isAnyAssignedToMe(assignees)
            }

            AssigneesData(
                assignees = assignees.toImmutableList(),
                isAssignedToMe = isAssignedToMe,
                newVersion = patchedData.newVersion
            )
        }
    }

    suspend fun updateWatchersData(
        version: Long,
        issueId: Long,
        newList: ImmutableList<Long>
    ): Result<WatchersListUpdateData> = resultOf {
        coroutineScope {
            val payload = mapOf("watchers" to newList).toPersistentMap()
            val patchedData = issuesRepository.patchData(
                version = version,
                issueId = issueId,
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

    /**
     * What they do on taiga-front:
     * 1. issues/by_ref?project=1&ref=19
     * 2. issues/attachments?object_id=8&project=1
     * 3. milestones/2
     * 4. issues/custom-attributes-values/8
     * 5. history/issue/8?type=comment
     * 6. history/issue/8?page=1&type=activity <- It is the Activities tab
     */
    suspend fun getIssueData(issueId: Long): Result<IssueDetailsData> = resultOf {
        coroutineScope {
            val filtersData = filtersRepository.getFiltersData(CommonTaskType.Issue)

            val issueDeferred = async {
                issuesRepository.getIssue(id = issueId, filtersData = filtersData)
            }

            val attachments = async { issuesRepository.getIssueAttachments(taskId = issueId) }

            val customFields = async {
                issuesRepository.getCustomFields(id = issueId)
            }
            val commentsDeferred = async {
                historyRepository.getComments(
                    commonTaskId = issueId,
                    type = CommonTaskType.Issue
                )
            }

            val issue = issueDeferred.await()

            val sprint = async {
                issue.milestone?.let { sprintsRepository.getSprint(sprintId = it) }
            }

            val creator = async { usersRepository.getUser(issue.creatorId) }

            val assigneesDeferred = async { usersRepository.getUsersList(issue.assignedUserIds) }
            val watchersDeferred = async { usersRepository.getUsersList(issue.watcherUserIds) }

            val assignees = assigneesDeferred.await()
            val watchers = watchersDeferred.await()

            val isAssignedToMe = async { usersRepository.isAnyAssignedToMe(assignees) }
            val isWatchedByMe = async { usersRepository.isAnyAssignedToMe(watchers) }

            IssueDetailsData(
                issueTask = issue,
                attachments = attachments.await().toImmutableList(),
                sprint = sprint.await(),
                customFields = customFields.await(),
                comments = commentsDeferred.await(),
                creator = creator.await(),
                assignees = assignees.toImmutableList(),
                watchers = watchers.toImmutableList(),
                isAssignedToMe = isAssignedToMe.await(),
                isWatchedByMe = isWatchedByMe.await(),
                filtersData = filtersData
            )
        }
    }
}
