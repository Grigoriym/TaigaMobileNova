package com.grappim.taigamobile.feature.swimlanes.data

import com.grappim.taigamobile.core.storage.KmpTaigaSessionStorage
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.feature.swimlanes.domain.SwimlanesRepository
import kotlinx.collections.immutable.ImmutableList
import org.koin.core.annotation.Single

@Single(binds = [SwimlanesRepository::class])
class SwimlanesRepositoryImpl(
    private val swimlanesApi: SwimlanesApi,
    private val taigaSessionStorage: KmpTaigaSessionStorage,
    private val swimlanesMapper: SwimlanesMapper
) : SwimlanesRepository {
    override suspend fun getSwimlanes(): ImmutableList<Swimlane> {
        val response = swimlanesApi.getSwimlanes(taigaSessionStorage.getCurrentProjectId())
        return swimlanesMapper.toListDomain(response)
    }
}
