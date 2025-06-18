package io.eugenethedev.taigamobile.epics

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import io.eugenethedev.taigamobile.domain.entities.CommonTask
import io.eugenethedev.taigamobile.domain.entities.FiltersData
import io.eugenethedev.taigamobile.state.Session
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EpicsRepositoryImpl @Inject constructor(
    private val epicsApi: EpicsApi,
    private val session: Session
) : EpicsRepository {
    override fun getEpics(filters: FiltersData): Flow<PagingData<CommonTask>> =
        Pager(
            PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            )
        ) {
            EpicsPagingSource(epicsApi, filters, session)
        }.flow
}
