package com.grappim.taigamobile.feature.sprint.data

import com.grappim.taigamobile.feature.sprint.domain.Sprint
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.core.annotation.Factory

@Factory
class SprintMapper {

    fun toDomainList(dto: List<SprintResponseDTO>): ImmutableList<Sprint> = dto.map { toDomain(it) }.toImmutableList()

    fun toDomain(dto: SprintResponseDTO): Sprint = Sprint(
        id = dto.id,
        name = dto.name,
        order = dto.order,
        start = dto.estimatedStart,
        end = dto.estimatedFinish,
        storiesCount = dto.userStories.size,
        isClosed = dto.closed
    )
}
