package com.grappim.taigamobile.feature.dashboard.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetMyWorkItemsUseCase @Inject constructor(private val workItemRepository: WorkItemRepository) {
    suspend fun getData(userId: Long, projectId: Long): Result<List<WorkItem>> = resultOf {
        coroutineScope {
            val epics = async {
                workItemRepository.getWorkItems(
                    commonTaskType = CommonTaskType.Epic,
                    projectId = projectId,
                    assignedId = userId,
                    isClosed = false,
                    isBlocked = false,
                    pageSize = 5
                )
            }

            val stories = async {
                workItemRepository.getWorkItems(
                    commonTaskType = CommonTaskType.UserStory,
                    projectId = projectId,
                    assignedId = userId,
                    isClosed = false,
                    isDashboard = true,
                    isBlocked = false,
                    pageSize = 10
                )
            }

            val tasks = async {
                workItemRepository.getWorkItems(
                    commonTaskType = CommonTaskType.Task,
                    projectId = projectId,
                    assignedId = userId,
                    isClosed = false,
                    isBlocked = false,
                    pageSize = 10
                )
            }

            val issues = async {
                workItemRepository.getWorkItems(
                    commonTaskType = CommonTaskType.Issue,
                    projectId = projectId,
                    assignedIds = userId.toString(),
                    isClosed = false,
                    isBlocked = false,
                    pageSize = 10
                )
            }

            (epics.await() + stories.await() + tasks.await() + issues.await()).toImmutableList()
        }
    }
}
