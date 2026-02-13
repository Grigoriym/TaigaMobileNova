package com.grappim.taigamobile.feature.dashboard.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import org.koin.core.annotation.Factory

@Factory
class GetRecentlyCompletedItemsUseCase(
    private val workItemRepository: WorkItemRepository,
    private val dateTimeUtils: DateTimeUtils
) {
    suspend fun getData(projectId: Long): Result<List<WorkItem>> = resultOf {
        val threeDaysAgo = dateTimeUtils.getLocalDateNow().minus(3, DateTimeUnit.DAY).toString()

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
