package com.grappim.taigamobile.feature.epics.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.db.dao.WorkItemDao
import com.grappim.taigamobile.feature.epics.domain.Epic
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import com.grappim.taigamobile.feature.epics.dto.LinkToEpicRequestDTO
import com.grappim.taigamobile.feature.epics.mapper.EpicMapper
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.data.WorkItemEntityMapper
import com.grappim.taigamobile.feature.workitem.data.WorkItemRemoteMediator
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EpicsRepositoryImpl @Inject constructor(
    private val epicsApi: EpicsApi,
    private val taigaSessionStorage: TaigaSessionStorage,
    private val workItemApi: WorkItemApi,
    private val epicMapper: EpicMapper,
    private val workItemMapper: WorkItemMapper,
    private val workItemEntityMapper: WorkItemEntityMapper,
    private val workItemDao: WorkItemDao
) : EpicsRepository {

    private var epicsPagingSource: EpicsPagingSource? = null

    @OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
    override fun getEpicsPaging(filters: FiltersData, query: String): Flow<PagingData<WorkItem>> {
        val hasFilters = filters.filtersNumber > 0 || query.isNotBlank()
        if (hasFilters) {
            return Pager(
                config = PagingConfig(
                    pageSize = 10,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    EpicsPagingSource(
                        filters = filters,
                        taigaSessionStorage = taigaSessionStorage,
                        query = query,
                        workItemApi = workItemApi,
                        workItemMapper = workItemMapper
                    ).also {
                        epicsPagingSource = it
                    }
                }
            ).flow
        }
        return taigaSessionStorage.currentProjectIdFlow.flatMapLatest { projectId ->
            Pager(
                config = PagingConfig(
                    pageSize = 10,
                    enablePlaceholders = false
                ),
                remoteMediator = WorkItemRemoteMediator(
                    taskType = CommonTaskType.Epic,
                    workItemApi = workItemApi,
                    workItemDao = workItemDao,
                    workItemMapper = workItemMapper,
                    workItemEntityMapper = workItemEntityMapper,
                    taigaSessionStorage = taigaSessionStorage
                ),
                pagingSourceFactory = { workItemDao.pagingSource(projectId, CommonTaskType.Epic) }
            ).flow.map { pagingData ->
                pagingData.map { entity -> workItemEntityMapper.toDomain(entity) }
            }
        }
    }

    override fun refreshEpics() {
        epicsPagingSource?.invalidate()
    }

    override suspend fun getEpics(
        projectId: Long,
        assignedId: Long?,
        isClosed: Boolean?,
        watcherId: Long?
    ): ImmutableList<Epic> {
        val response = workItemApi.getWorkItems(
            taskPath = WorkItemPathPlural(CommonTaskType.Epic),
            project = projectId,
            assignedId = assignedId,
            isClosed = isClosed,
            watcherId = watcherId
        )
        return epicMapper.toDomainList(response)
    }

    override suspend fun linkToEpic(epicId: Long, userStoryId: Long) = epicsApi.linkToEpic(
        epicId = epicId,
        linkToEpicRequest = LinkToEpicRequestDTO(epicId.toString(), userStoryId)
    )

    override suspend fun unlinkFromEpic(epicId: Long, userStoryId: Long) {
        epicsApi.unlinkFromEpic(epicId, userStoryId)
    }

    override suspend fun getEpic(id: Long): Epic {
        val response = workItemApi.getWorkItemById(
            taskPath = WorkItemPathPlural(CommonTaskType.Epic),
            id = id
        )
        return epicMapper.toDomain(resp = response)
    }
}
