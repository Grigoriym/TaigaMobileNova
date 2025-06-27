package com.grappim.taigamobile.feature.swimlanes.domain

import com.grappim.taigamobile.core.domain.Swimlane

interface SwimlanesRepository {
    suspend fun getSwimlanes(): List<Swimlane>
}
