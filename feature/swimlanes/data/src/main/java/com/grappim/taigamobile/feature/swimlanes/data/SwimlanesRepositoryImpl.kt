package com.grappim.taigamobile.feature.swimlanes.data

import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.feature.swimlanes.domain.SwimlanesRepository
import kotlinx.collections.immutable.ImmutableList
import javax.inject.Inject

class SwimlanesRepositoryImpl @Inject constructor(
    private val swimlanesApi: SwimlanesApi,
    private val taigaSessionStorage: TaigaSessionStorage,
    private val swimlanesMapper: SwimlanesMapper
) : SwimlanesRepository {
    override suspend fun getSwimlanes(): ImmutableList<Swimlane> {
        val response = swimlanesApi.getSwimlanes(taigaSessionStorage.getCurrentProjectId())
        return swimlanesMapper.toListDomain(response)
    }
}
