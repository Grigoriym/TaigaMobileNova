package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.sprint.data.SprintResponseDTO
import com.grappim.taigamobile.feature.sprint.data.SprintUserStoryDTO
import com.grappim.taigamobile.feature.sprint.domain.Sprint
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
