package com.grappim.taigamobile.feature.issues.domain

import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.patch.PatchedCustomAttributes
import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toImmutableList
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

    suspend fun deleteAttachment(attachment: Attachment): Result<Unit> = resultOf {
        issuesRepository.deleteAttachment(attachment)
    }

    suspend fun addAttachment(
        issueId: Long,
        fileName: String,
        fileByteArray: ByteArray
    ): Result<Attachment> = resultOf {
        issuesRepository.addAttachment(
            issueId = issueId,
            fileName = fileName,
            fileByteArray = fileByteArray
        )
    }

    suspend fun patchCustomAttributes(
        version: Long,
        issueId: Long,
        payload: PersistentMap<String, Any?>
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
        payload: PersistentMap<String, Any?>
    ): Result<PatchedData> = resultOf {
        issuesRepository.patchData(version = version, issueId = issueId, payload = payload)
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
    suspend fun getIssueData(taskId: Long, ref: Int): Result<IssueDetailsData> = resultOf {
        coroutineScope {
            val filtersData = filtersRepository.getFiltersData(CommonTaskType.Issue)

            val issueDeferred = async {
                issuesRepository.getIssueByRef(ref = ref, filtersData = filtersData)
            }

            val attachments = async { issuesRepository.getIssueAttachments(taskId = taskId) }

            val customFields = async {
                issuesRepository.getCustomFields(commonTaskId = taskId)
            }
            val commentsDeferred = async {
                historyRepository.getComments(
                    commonTaskId = taskId,
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
                comments = commentsDeferred.await().toImmutableList(),
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
