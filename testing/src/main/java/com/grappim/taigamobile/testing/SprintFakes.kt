package com.grappim.taigamobile.testing

import com.grappim.taigamobile.core.domain.Sprint

fun getSprint(): Sprint = Sprint(
    id = getRandomLong(),
    name = getRandomString(),
    order = getRandomInt(),
    start = nowLocalDate,
    end = nowLocalDate,
    storiesCount = getRandomInt(),
    isClosed = getRandomBoolean()
)
