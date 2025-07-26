package com.grappim.taigamobile.feature.swimlanes.domain

import com.grappim.taigamobile.core.domain.SwimlaneDTO

interface SwimlanesRepository {
    suspend fun getSwimlanes(): List<SwimlaneDTO>
}
