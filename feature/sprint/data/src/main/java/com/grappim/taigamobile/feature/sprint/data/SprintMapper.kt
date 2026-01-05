package com.grappim.taigamobile.feature.sprint.data

import com.grappim.taigamobile.core.async.DefaultDispatcher
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SprintMapper @Inject constructor(@DefaultDispatcher private val dispatcher: CoroutineDispatcher) {

    suspend fun toDomainList(dto: List<SprintResponseDTO>): ImmutableList<Sprint> =
        dto.map { toDomain(it) }.toImmutableList()

    suspend fun toDomain(dto: SprintResponseDTO): Sprint = withContext(dispatcher) {
        Sprint(
            id = dto.id,
            name = dto.name,
            order = dto.order,
            start = dto.estimatedStart,
            end = dto.estimatedFinish,
            storiesCount = dto.userStories.size,
            isClosed = dto.closed
        )
    }
}
