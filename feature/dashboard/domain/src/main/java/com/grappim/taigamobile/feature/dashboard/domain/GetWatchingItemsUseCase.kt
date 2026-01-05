package com.grappim.taigamobile.feature.dashboard.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetWatchingItemsUseCase @Inject constructor(private val workItemRepository: WorkItemRepository) {
    suspend fun getData(userId: Long, projectId: Long): Result<List<WorkItem>> = resultOf {
        coroutineScope {
            val epics = async {
                workItemRepository.getWorkItems(
                    commonTaskType = CommonTaskType.Epic,
                    projectId = projectId,
                    watcherId = userId,
                    isClosed = false,
                    pageSize = 10
                )
            }

            val stories = async {
                workItemRepository.getWorkItems(
                    commonTaskType = CommonTaskType.UserStory,
                    projectId = projectId,
                    watcherId = userId,
                    isClosed = false,
                    pageSize = 10
                )
            }

            val tasks = async {
                workItemRepository.getWorkItems(
                    commonTaskType = CommonTaskType.Task,
                    projectId = projectId,
                    watcherId = userId,
                    isClosed = false,
                    pageSize = 10
                )
            }

            val issues = async {
                workItemRepository.getWorkItems(
                    commonTaskType = CommonTaskType.Issue,
                    projectId = projectId,
                    watcherId = userId,
                    isClosed = false,
                    pageSize = 10
                )
            }

            (epics.await() + stories.await() + tasks.await() + issues.await()).toImmutableList()
        }
    }
}
