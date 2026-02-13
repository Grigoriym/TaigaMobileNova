package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.swimlanes.data.SwimlaneDTO
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane

fun getSwimlaneDTO(): SwimlaneDTO = SwimlaneDTO(
    id = getRandomLong(),
    name = getRandomString(),
    order = getRandomLong()
)

fun getSwimlane(): Swimlane = Swimlane(
    id = getRandomLong(),
    name = getRandomString(),
    order = getRandomLong()
)
