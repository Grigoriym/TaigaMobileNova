package com.grappim.taigamobile.feature.swimlanes.data

import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.core.annotation.Factory

@Factory
class SwimlanesMapper {

    fun toListDomain(dtos: List<SwimlaneDTO>): ImmutableList<Swimlane> = dtos.map { toDomain(it) }.toImmutableList()

    fun toDomain(dto: SwimlaneDTO): Swimlane = Swimlane(
        id = dto.id,
        name = dto.name,
        order = dto.order
    )
}
