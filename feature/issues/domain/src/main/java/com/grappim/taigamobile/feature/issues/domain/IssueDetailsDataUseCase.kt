package com.grappim.taigamobile.feature.issues.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.Sprint
import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class IssueDetailsDataUseCase @Inject constructor(
    private val issuesRepository: IssuesRepository,
    private val sprintsRepository: SprintsRepository,
    private val historyRepository: HistoryRepository,
    private val usersRepository: UsersRepository,
    private val filtersRepository: FiltersRepository,
    private val workItemRepository: WorkItemRepository,
    private val patchDataGenerator: PatchDataGenerator
) {

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
            val taskType = CommonTaskType.Issue
            val filtersData = filtersRepository.getFiltersData(taskType)

            val issueDeferred = async {
                issuesRepository.getIssue(id = issueId, filtersData = filtersData)
            }

            val attachments = async {
                workItemRepository.getWorkItemAttachments(
                    workItemId = issueId,
                    commonTaskType = taskType
                )
            }

            val customFields = async {
                workItemRepository.getCustomFields(
                    workItemId = issueId,
                    commonTaskType = taskType
                )
            }
            val commentsDeferred = async {
                historyRepository.getComments(
                    commonTaskId = issueId,
                    type = taskType
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
                issue = issue,
                attachments = attachments.await(),
                sprint = sprint.await(),
                customFields = customFields.await(),
                comments = commentsDeferred.await(),
                creator = creator.await(),
                assignees = assignees,
                watchers = watchers,
                isAssignedToMe = isAssignedToMe.await(),
                isWatchedByMe = isWatchedByMe.await(),
                filtersData = filtersData
            )
        }
    }

    suspend fun updateSprint(sprintId: Long?, version: Long, workItemId: Long, commonTaskType: CommonTaskType) =
        resultOf {
            val patchedData = workItemRepository.patchData(
                version = version,
                workItemId = workItemId,
                commonTaskType = commonTaskType,
                payload = patchDataGenerator.getSprint(
                    sprintId = sprintId
                )
            )
            var sprint: Sprint? = null
            if (sprintId != null) {
                sprint = sprintsRepository.getSprint(sprintId)
            }
            UpdateSprintData(
                patchedData = patchedData,
                sprint = sprint
            )
        }
}

data class UpdateSprintData(val patchedData: PatchedData, val sprint: Sprint?)
