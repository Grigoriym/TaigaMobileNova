package com.grappim.taigamobile.feature.sprint.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.grappim.taigamobile.core.api.hasNextPage
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.db.dao.SprintDao
import com.grappim.taigamobile.core.storage.db.entities.SprintEntity

private const val PAGE_SIZE = 10

@OptIn(ExperimentalPagingApi::class)
class SprintRemoteMediator(
    private val isClosed: Boolean,
    private val sprintApi: SprintApi,
    private val sprintDao: SprintDao,
    private val sprintMapper: SprintMapper,
    private val taigaSessionStorage: TaigaSessionStorage
) : RemoteMediator<Int, SprintEntity>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, SprintEntity>): MediatorResult {
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
                        // Calculate page based on current item count
                        val currentCount = state.pages.sumOf { it.data.size }
                        (currentCount / PAGE_SIZE) + 1
                    }
                }
            }

            val response = sprintApi.getSprintsPaging(
                project = projectId,
                page = page,
                isClosed = isClosed
            )

            val sprints = sprintMapper.toDomainList(response.body() ?: emptyList())
            val entities = sprints.map { sprint ->
                SprintEntity(
                    id = sprint.id,
                    projectId = projectId,
                    name = sprint.name,
                    order = sprint.order,
                    start = sprint.start,
                    end = sprint.end,
                    storiesCount = sprint.storiesCount,
                    isClosed = sprint.isClosed
                )
            }

            if (loadType == LoadType.REFRESH) {
                sprintDao.deleteByProjectIdAndClosed(projectId, isClosed)
            }

            sprintDao.insertAll(entities)

            MediatorResult.Success(endOfPaginationReached = !response.hasNextPage())
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
