package com.grappim.taigamobile.testing.repo

import androidx.paging.PagingData
import com.grappim.taigamobile.feature.epics.domain.Epic
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

class FakeEpicsRepository : EpicsRepository {
    override fun getEpicsPaging(
        filters: FiltersData,
        query: String
    ): Flow<PagingData<WorkItem>> {
        TODO("Not yet implemented")
    }

    override suspend fun getEpics(
        projectId: Long,
        assignedId: Long?,
        isClosed: Boolean?,
        watcherId: Long?
    ): ImmutableList<Epic> {
        TODO("Not yet implemented")
    }

    override suspend fun linkToEpic(epicId: Long, userStoryId: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun unlinkFromEpic(epicId: Long, userStoryId: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun getEpic(id: Long): Epic {
        TODO("Not yet implemented")
    }
}