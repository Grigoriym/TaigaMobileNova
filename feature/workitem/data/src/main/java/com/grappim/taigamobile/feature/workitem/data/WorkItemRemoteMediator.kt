package com.grappim.taigamobile.feature.workitem.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.grappim.taigamobile.core.api.hasNextPage
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.db.dao.WorkItemDao
import com.grappim.taigamobile.core.storage.db.entities.WorkItemEntity
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper

private const val PAGE_SIZE = 10

@OptIn(ExperimentalPagingApi::class)
class WorkItemRemoteMediator(
    private val taskType: CommonTaskType,
    private val workItemApi: WorkItemApi,
    private val workItemDao: WorkItemDao,
    private val workItemMapper: WorkItemMapper,
    private val workItemEntityMapper: WorkItemEntityMapper,
    private val taigaSessionStorage: TaigaSessionStorage
) : RemoteMediator<Int, WorkItemEntity>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, WorkItemEntity>): MediatorResult {
        return try {
            val projectId = taigaSessionStorage.getCurrentProjectId()

            val page = when (loadType) {
                LoadType.REFRESH -> 1

                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)

                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    if (lastItem == null) {
                        1
                    } else {
                        val currentCount = state.pages.sumOf { it.data.size }
                        (currentCount / PAGE_SIZE) + 1
                    }
                }
            }

            val sprint = if (taskType == CommonTaskType.UserStory) "null" else null

            val response = workItemApi.getWorkItemsPagination(
                taskPath = WorkItemPathPlural(taskType),
                project = projectId,
                page = page,
                pageSize = PAGE_SIZE,
                sprint = sprint
            )

            val items = workItemMapper.toDomainList(response.body() ?: emptyList(), taskType)
            val entities = workItemEntityMapper.toEntityList(items)

            if (loadType == LoadType.REFRESH) {
                workItemDao.deleteByProjectIdAndType(projectId, taskType)
            }

            workItemDao.insertAll(entities)

            MediatorResult.Success(endOfPaginationReached = !response.hasNextPage())
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
