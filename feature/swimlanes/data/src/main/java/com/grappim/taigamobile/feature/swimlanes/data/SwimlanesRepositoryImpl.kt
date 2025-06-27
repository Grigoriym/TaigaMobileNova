package com.grappim.taigamobile.feature.swimlanes.data

import com.grappim.taigamobile.core.domain.Swimlane
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.swimlanes.domain.SwimlanesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SwimlanesRepositoryImpl @Inject constructor(
    private val swimlanesApi: SwimlanesApi,
    private val taigaStorage: TaigaStorage
) : SwimlanesRepository {
    override suspend fun getSwimlanes(): List<Swimlane> =
        swimlanesApi.getSwimlanes(taigaStorage.currentProjectIdFlow.first())
}
