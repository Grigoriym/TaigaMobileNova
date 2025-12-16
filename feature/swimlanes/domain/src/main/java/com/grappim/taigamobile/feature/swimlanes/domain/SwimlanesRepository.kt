package com.grappim.taigamobile.feature.swimlanes.domain

import kotlinx.collections.immutable.ImmutableList

interface SwimlanesRepository {
    suspend fun getSwimlanes(): ImmutableList<Swimlane>
}
