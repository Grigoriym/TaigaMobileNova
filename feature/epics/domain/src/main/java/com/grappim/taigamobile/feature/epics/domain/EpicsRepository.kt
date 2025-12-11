package com.grappim.taigamobile.feature.epics.domain

import androidx.paging.PagingData
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface EpicsRepository {
    fun getEpicsPaging(filters: FiltersDataDTO): Flow<PagingData<CommonTask>>
    fun refreshEpics()

    suspend fun getEpicsOld(
        assignedId: Long? = null,
        isClosed: Boolean? = null,
        watcherId: Long? = null
    ): List<CommonTask>

    suspend fun getEpics(isClosed: Boolean? = false): ImmutableList<Epic>
    suspend fun linkToEpic(epicId: Long, userStoryId: Long)
    suspend fun unlinkFromEpic(epicId: Long, userStoryId: Long)
    suspend fun getEpic(id: Long): Epic
}
