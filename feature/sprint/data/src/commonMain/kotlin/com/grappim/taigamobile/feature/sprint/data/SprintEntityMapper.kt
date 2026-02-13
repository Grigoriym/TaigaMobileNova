package com.grappim.taigamobile.feature.sprint.data

import com.grappim.taigamobile.core.storage.db.entities.SprintEntity
import com.grappim.taigamobile.feature.sprint.domain.Sprint

internal fun SprintEntity.toDomain(): Sprint = Sprint(
    id = id,
    name = name,
    order = order,
    start = start,
    end = end,
    storiesCount = storiesCount,
    isClosed = isClosed
)

internal fun Sprint.toEntity(projectId: Long): SprintEntity = SprintEntity(
    id = id,
    projectId = projectId,
    name = name,
    order = order,
    start = start,
    end = end,
    storiesCount = storiesCount,
    isClosed = isClosed
)

internal fun List<SprintEntity>.toDomainList(): List<Sprint> = map { it.toDomain() }

internal fun List<Sprint>.toEntityList(projectId: Long): List<SprintEntity> = map { it.toEntity(projectId) }
