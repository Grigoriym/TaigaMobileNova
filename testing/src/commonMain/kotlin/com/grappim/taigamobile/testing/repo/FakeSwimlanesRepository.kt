package com.grappim.taigamobile.testing.repo

import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.feature.swimlanes.domain.SwimlanesRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

class FakeSwimlanesRepository : SwimlanesRepository {

    var getSwimlanesResult: ImmutableList<Swimlane> = persistentListOf()
    var getSwimlanesThrows: Throwable? = null

    override suspend fun getSwimlanes(): ImmutableList<Swimlane> {
        getSwimlanesThrows?.let { throw it }
        return getSwimlanesResult
    }
}
