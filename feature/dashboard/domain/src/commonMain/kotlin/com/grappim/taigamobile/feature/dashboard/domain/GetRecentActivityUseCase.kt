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

interface GetRecentActivityUseCase {
    suspend fun getData(projectId: Long): Result<List<WorkItem>>
}

@Factory(binds = [GetRecentActivityUseCase::class])
class GetRecentActivityUseCaseImpl(
    private val workItemRepository: WorkItemRepository,
    private val dateTimeUtils: DateTimeUtils
) : GetRecentActivityUseCase {
    override suspend fun getData(projectId: Long): Result<List<WorkItem>> = resultOf {
        val threeDaysAgo = dateTimeUtils.getLocalDateNow().minus(3, DateTimeUnit.DAY).toString()

        coroutineScope {
            val stories = async {
                workItemRepository.getWorkItems(
                    commonTaskType = CommonTaskType.UserStory,
                    projectId = projectId,
                    isClosed = false,
                    modifiedDateGte = threeDaysAgo,
                    pageSize = 10
                )
            }

            val tasks = async {
                workItemRepository.getWorkItems(
                    commonTaskType = CommonTaskType.Task,
                    projectId = projectId,
                    isClosed = false,
                    modifiedDateGte = threeDaysAgo,
                    pageSize = 10
                )
            }

            (stories.await() + tasks.await())
                .sortedByDescending { it.createdDate }
                .toImmutableList()
        }
    }
}
