package com.grappim.taigamobile.feature.sprint.data

import com.grappim.taigamobile.core.domain.Sprint

fun SprintResponse.toSprint() = Sprint(
    id = id,
    name = name,
    order = order,
    start = estimated_start,
    end = estimated_finish,
    storiesCount = user_stories.size,
    isClosed = closed
)
