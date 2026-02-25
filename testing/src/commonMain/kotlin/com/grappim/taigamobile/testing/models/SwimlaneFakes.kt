package com.grappim.taigamobile.testing.models

import com.grappim.taigamobile.feature.swimlanes.data.SwimlaneDTO
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString

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
