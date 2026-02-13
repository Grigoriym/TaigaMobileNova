package com.grappim.taigamobile.feature.epics.domain

import androidx.paging.PagingData
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface EpicsRepository {
    fun getEpicsPaging(filters: FiltersData, query: String): Flow<PagingData<WorkItem>>

    suspend fun getEpics(
        projectId: Long,
        assignedId: Long? = null,
        isClosed: Boolean? = null,
        watcherId: Long? = null
    ): ImmutableList<Epic>
    suspend fun linkToEpic(epicId: Long, userStoryId: Long)
    suspend fun unlinkFromEpic(epicId: Long, userStoryId: Long)
    suspend fun getEpic(id: Long): Epic
}
