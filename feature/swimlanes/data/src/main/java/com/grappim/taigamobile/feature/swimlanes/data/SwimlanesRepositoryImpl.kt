package com.grappim.taigamobile.feature.swimlanes.data

import com.grappim.taigamobile.core.domain.SwimlaneDTO
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.feature.swimlanes.domain.SwimlanesRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SwimlanesRepositoryImpl @Inject constructor(
    private val swimlanesApi: SwimlanesApi,
    private val taigaStorage: TaigaStorage,
    private val swimlanesMapper: SwimlanesMapper
) : SwimlanesRepository {
    override suspend fun getSwimlanes(): ImmutableList<Swimlane> {
        val response = swimlanesApi.getSwimlanes(taigaStorage.currentProjectIdFlow.first())
        return swimlanesMapper.toListDomain(response)
    }

    override suspend fun getSwimlanesOld(): List<SwimlaneDTO> =
        swimlanesApi.getSwimlanes(taigaStorage.currentProjectIdFlow.first())
}
