package com.grappim.taigamobile.testing.models

import com.grappim.taigamobile.core.storage.db.entities.SprintEntity
import com.grappim.taigamobile.feature.sprint.data.SprintResponseDTO
import com.grappim.taigamobile.feature.sprint.data.SprintUserStoryDTO
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.testing.utils.getRandomBoolean
import com.grappim.taigamobile.testing.utils.getRandomInt
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.nowLocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

fun getSprint(): Sprint = Sprint(
    id = getRandomLong(),
    name = getRandomString(),
    order = getRandomInt(),
    start = nowLocalDate,
    end = nowLocalDate,
    storiesCount = getRandomInt(),
    isClosed = getRandomBoolean()
)

fun getSprintResponseDTO(): SprintResponseDTO = SprintResponseDTO(
    id = getRandomLong(),
    name = getRandomString(),
    estimatedStart = nowLocalDate,
    estimatedFinish = nowLocalDate.plus(14, DateTimeUnit.DAY),
    closed = false,
    order = 1,
    userStories = listOf(SprintUserStoryDTO(id = getRandomLong()))
)

fun getSprintEntity(
    isClosed: Boolean = getRandomBoolean(),
    projectId: Long = getRandomLong()
): SprintEntity = SprintEntity(
    id = getRandomLong(),
    projectId = projectId,
    name = getRandomString(),
    order = getRandomInt(),
    start = nowLocalDate,
    end = nowLocalDate.plus(14, DateTimeUnit.DAY),
    storiesCount = getRandomInt(),
    isClosed = isClosed
)
