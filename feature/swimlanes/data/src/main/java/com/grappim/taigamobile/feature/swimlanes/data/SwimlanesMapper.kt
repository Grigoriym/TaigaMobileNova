package com.grappim.taigamobile.feature.swimlanes.data

import com.grappim.taigamobile.core.async.DefaultDispatcher
import com.grappim.taigamobile.feature.swimlanes.data.SwimlaneDTO
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SwimlanesMapper @Inject constructor(@DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher) {

    suspend fun toListDomain(dtos: List<SwimlaneDTO>): ImmutableList<Swimlane> = withContext(defaultDispatcher) {
        dtos.map { toDomain(it) }.toImmutableList()
    }

    suspend fun toDomain(dto: SwimlaneDTO): Swimlane = withContext(defaultDispatcher) {
        Swimlane(
            id = dto.id,
            name = dto.name,
            order = dto.order
        )
    }
}
