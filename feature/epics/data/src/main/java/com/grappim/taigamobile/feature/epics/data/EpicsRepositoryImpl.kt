package com.grappim.taigamobile.feature.epics.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.api.toCommonTask
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EpicsRepositoryImpl @Inject constructor(private val epicsApi: EpicsApi, private val taigaStorage: TaigaStorage) :
    EpicsRepository {

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

    override suspend fun getEpics(assignedId: Long?, isClosed: Boolean?, watcherId: Long?): List<CommonTask> =
        epicsApi.getEpics(
            assignedId = assignedId,
            isClosed = isClosed,
            watcherId = watcherId
        ).map { it.toCommonTask(CommonTaskType.Epic) }

    override suspend fun linkToEpic(epicId: Long, userStoryId: Long) = epicsApi.linkToEpic(
        epicId = epicId,
        linkToEpicRequest = LinkToEpicRequest(epicId.toString(), userStoryId)
    )

    override suspend fun unlinkFromEpic(epicId: Long, userStoryId: Long) {
        epicsApi.unlinkFromEpic(epicId, userStoryId)
    }
}
