package com.grappim.taigamobile.feature.epics.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.epics.domain.Epic
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import com.grappim.taigamobile.feature.epics.dto.LinkToEpicRequestDTO
import com.grappim.taigamobile.feature.epics.mapper.EpicMapper
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.getPluralPath
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single(binds = [EpicsRepository::class])
class EpicsRepositoryImpl(
    private val epicsApi: EpicsApi,
    private val taigaSessionStorage: TaigaSessionStorage,
    private val workItemApi: WorkItemApi,
    private val epicMapper: EpicMapper,
    private val workItemMapper: WorkItemMapper,
) : EpicsRepository {

    override fun getEpicsPaging(filters: FiltersData, query: String): Flow<PagingData<WorkItem>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                EpicsPagingSource(
                    filters = filters,
                    taigaSessionStorage = taigaSessionStorage,
                    query = query,
                    workItemApi = workItemApi,
                    workItemMapper = workItemMapper
                )
            }
        ).flow
    }

    override suspend fun getEpics(
        projectId: Long,
        assignedId: Long?,
        isClosed: Boolean?,
        watcherId: Long?
    ): ImmutableList<Epic> {
        val response = workItemApi.getWorkItems(
            taskPath = CommonTaskType.Epic.getPluralPath(),
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
            taskPath = CommonTaskType.Epic.getPluralPath(),
            id = id
        )
        return epicMapper.toDomain(resp = response)
    }
}
