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

data class EpicLinkCall(val epicId: Long, val userStoryId: Long)

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

    var linkToEpicThrows: Throwable? = null
    val linkToEpicCalls: MutableList<EpicLinkCall> = mutableListOf()

    override suspend fun linkToEpic(epicId: Long, userStoryId: Long) {
        linkToEpicCalls += EpicLinkCall(epicId = epicId, userStoryId = userStoryId)
        linkToEpicThrows?.let { throw it }
    }

    var unlinkFromEpicThrows: Throwable? = null
    val unlinkFromEpicCalls: MutableList<EpicLinkCall> = mutableListOf()

    override suspend fun unlinkFromEpic(epicId: Long, userStoryId: Long) {
        unlinkFromEpicCalls += EpicLinkCall(epicId = epicId, userStoryId = userStoryId)
        unlinkFromEpicThrows?.let { throw it }
    }

    override suspend fun getEpic(id: Long): Epic {
        getEpicThrows?.let { throw it }
        return getEpicResult ?: error("getEpicResult not set")
    }
}
