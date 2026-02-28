package com.grappim.taigamobile.testing.repo

import androidx.paging.PagingData
import com.grappim.taigamobile.feature.epics.domain.Epic
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeEpicsRepository : EpicsRepository {

    var getEpicResult: Epic? = null
    var getEpicThrows: Throwable? = null

    override fun getEpicsPaging(
        filters: FiltersData,
        query: String
    ): Flow<PagingData<WorkItem>> = flowOf(PagingData.empty())

    var getEpicsResult: ImmutableList<Epic> = persistentListOf()
    var getEpicsThrows: Throwable? = null

    override suspend fun getEpics(
        projectId: Long,
        assignedId: Long?,
        isClosed: Boolean?,
        watcherId: Long?
    ): ImmutableList<Epic> {
        getEpicsThrows?.let { throw it }
        return getEpicsResult
    }

    override suspend fun linkToEpic(epicId: Long, userStoryId: Long) =
        error("not used in this test")

    override suspend fun unlinkFromEpic(epicId: Long, userStoryId: Long) =
        error("not used in this test")

    override suspend fun getEpic(id: Long): Epic {
        getEpicThrows?.let { throw it }
        return getEpicResult ?: error("getEpicResult not set")
    }
}
