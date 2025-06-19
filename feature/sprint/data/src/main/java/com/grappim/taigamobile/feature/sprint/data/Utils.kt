package com.grappim.taigamobile.feature.sprint.data

import com.grappim.taigamobile.core.domain.Sprint

fun SprintResponse.toSprint() = Sprint(
    id = id,
    name = name,
    order = order,
    start = estimatedStart,
    end = estimatedFinish,
    storiesCount = userStories.size,
    isClosed = closed
)
