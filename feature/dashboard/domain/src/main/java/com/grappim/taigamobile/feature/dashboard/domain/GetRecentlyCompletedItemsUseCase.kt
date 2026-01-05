package com.grappim.taigamobile.feature.dashboard.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate
import javax.inject.Inject

class GetRecentlyCompletedItemsUseCase @Inject constructor(private val workItemRepository: WorkItemRepository) {
    suspend fun getData(projectId: Long): Result<List<WorkItem>> = resultOf {
        val threeDaysAgo = LocalDate.now().minusDays(3).toString()

        coroutineScope {
            val stories = async {
                workItemRepository.getWorkItems(
                    commonTaskType = CommonTaskType.UserStory,
                    projectId = projectId,
                    isClosed = true,
                    finishDateGte = threeDaysAgo,
                    pageSize = 5
                )
            }

            val tasks = async {
                workItemRepository.getWorkItems(
                    commonTaskType = CommonTaskType.Task,
                    projectId = projectId,
                    isClosed = true,
                    finishDateGte = threeDaysAgo,
                    pageSize = 5
                )
            }

            val issues = async {
                workItemRepository.getWorkItems(
                    commonTaskType = CommonTaskType.Issue,
                    projectId = projectId,
                    isClosed = true,
                    finishDateGte = threeDaysAgo,
                    pageSize = 5
                )
            }

            (stories.await() + tasks.await() + issues.await())
                .sortedByDescending { it.createdDate }
                .toImmutableList()
        }
    }
}
