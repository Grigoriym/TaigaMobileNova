package com.grappim.taigamobile.feature.epics.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.FiltersData
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EpicsRepositoryImpl @Inject constructor(
    private val epicsApi: EpicsApi,
    private val session: Session
) : EpicsRepository {
    override fun getEpics(filters: FiltersData): Flow<PagingData<CommonTask>> = Pager(
        PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        )
    ) {
        EpicsPagingSource(epicsApi, filters, session)
    }.flow
}
