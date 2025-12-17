package com.grappim.taigamobile.feature.dashboard.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetDashboardDataUseCase @Inject constructor(private val workItemRepository: WorkItemRepository) {
    suspend fun getData(userId: Long, projectId: Long): Result<DashboardData> = resultOf {
        coroutineScope {
            val workingOn = async { getWorkingOn(userId, projectId) }
            val watching = async { getWatching(userId, projectId) }

            DashboardData(
                workingOn = workingOn.await().toImmutableList(),
                watching = watching.await().toImmutableList()
            )
        }
    }

    private suspend fun getWorkingOn(userId: Long, projectId: Long): List<WorkItem> = coroutineScope {
        val epics = async {
            workItemRepository.getWorkItems(
                commonTaskType = CommonTaskType.Epic,
                projectId = projectId,
                assignedId = userId,
                isClosed = false
            )
        }

        val stories = async {
            workItemRepository.getWorkItems(
                commonTaskType = CommonTaskType.UserStory,
                projectId = projectId,
                assignedId = userId,
                isClosed = false,
                isDashboard = true
            )
        }

        val tasks = async {
            workItemRepository.getWorkItems(
                commonTaskType = CommonTaskType.Task,
                projectId = projectId,
                assignedId = userId,
                isClosed = false
            )
        }

        val issues = async {
            workItemRepository.getWorkItems(
                commonTaskType = CommonTaskType.Issue,
                projectId = projectId,
                assignedIds = userId.toString(),
                isClosed = false
            )
        }
        epics.await() + stories.await() + tasks.await() + issues.await()
    }

    private suspend fun getWatching(userId: Long, projectId: Long): List<WorkItem> = coroutineScope {
        val epics = async {
            workItemRepository.getWorkItems(
                commonTaskType = CommonTaskType.Epic,
                projectId = projectId,
                watcherId = userId,
                isClosed = false
            )
        }

        val stories = async {
            workItemRepository.getWorkItems(
                commonTaskType = CommonTaskType.UserStory,
                projectId = projectId,
                watcherId = userId,
                isClosed = false,
                isDashboard = true
            )
        }

        val tasks = async {
            workItemRepository.getWorkItems(
                commonTaskType = CommonTaskType.Task,
                projectId = projectId,
                watcherId = userId,
                isClosed = false
            )
        }

        val issues = async {
            workItemRepository.getWorkItems(
                commonTaskType = CommonTaskType.Issue,
                projectId = projectId,
                watcherId = userId,
                isClosed = false
            )
        }

        epics.await() + stories.await() + tasks.await() + issues.await()
    }
}
