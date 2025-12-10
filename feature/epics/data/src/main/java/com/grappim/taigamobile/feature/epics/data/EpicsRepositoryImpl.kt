package com.grappim.taigamobile.feature.epics.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.api.toCommonTask
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.epics.domain.Epic
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EpicsRepositoryImpl @Inject constructor(
    private val epicsApi: EpicsApi,
    private val taigaStorage: TaigaStorage,
    private val workItemApi: WorkItemApi,
    private val epicMapper: EpicMapper
) : EpicsRepository {

    private var epicsPagingSource: EpicsPagingSource? = null

    override fun getEpicsPaging(filters: FiltersDataDTO): Flow<PagingData<CommonTask>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            EpicsPagingSource(epicsApi, filters, taigaStorage).also {
                epicsPagingSource = it
            }
        }
    ).flow

    // todo i really don't like it, move away from paging and create a custom one?
    // or maybe there is a more elegant way of doing refresh not from UI
    override fun refreshEpics() {
        epicsPagingSource?.invalidate()
    }

    override suspend fun getEpicsOld(assignedId: Long?, isClosed: Boolean?, watcherId: Long?): List<CommonTask> =
        epicsApi.getEpics(
            assignedId = assignedId,
            isClosed = isClosed,
            watcherId = watcherId
        ).map { it.toCommonTask(CommonTaskType.Epic) }

    override suspend fun getEpics(isClosed: Boolean?): ImmutableList<Epic> {
        val response = epicsApi.getEpics(isClosed = isClosed)
        val result = response.map { dto ->
            epicMapper.toDomainOld(dto)
        }
        return result.toImmutableList()
    }

    override suspend fun linkToEpic(epicId: Long, userStoryId: Long) = epicsApi.linkToEpic(
        epicId = epicId,
        linkToEpicRequest = LinkToEpicRequest(epicId.toString(), userStoryId)
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
