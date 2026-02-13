package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.sprint.data.SprintResponseDTO
import com.grappim.taigamobile.feature.sprint.data.SprintUserStoryDTO
import com.grappim.taigamobile.feature.sprint.domain.Sprint

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
    estimatedFinish = nowLocalDate.plusDays(14),
    closed = false,
    order = 1,
    userStories = listOf(SprintUserStoryDTO(id = getRandomLong()))
)
