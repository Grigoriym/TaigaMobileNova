package com.grappim.taigamobile.feature.swimlanes.domain

import com.grappim.taigamobile.core.domain.SwimlaneDTO
import kotlinx.collections.immutable.ImmutableList

interface SwimlanesRepository {
    suspend fun getSwimlanes(): ImmutableList<Swimlane>

    @Deprecated("remove it")
    suspend fun getSwimlanesOld(): List<SwimlaneDTO>
}
