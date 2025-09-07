package com.grappim.taigamobile.feature.swimlanes.data

import com.grappim.taigamobile.core.domain.SwimlaneDTO
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.swimlanes.domain.SwimlanesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SwimlanesRepositoryImpl @Inject constructor(
    private val swimlanesApi: SwimlanesApi,
    private val taigaStorage: TaigaStorage
) : SwimlanesRepository {
    override suspend fun getSwimlanes(): List<SwimlaneDTO> =
        swimlanesApi.getSwimlanes(taigaStorage.currentProjectIdFlow.first())
}
