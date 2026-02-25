package com.grappim.taigamobile.testing.api

import com.grappim.taigamobile.feature.swimlanes.data.SwimlaneDTO
import com.grappim.taigamobile.feature.swimlanes.data.SwimlanesApi

class FakeSwimlanesApi : SwimlanesApi {

    var swimlanesResult: List<SwimlaneDTO> = emptyList()
    var lastProjectId: Long? = null

    override suspend fun getSwimlanes(project: Long): List<SwimlaneDTO> {
        lastProjectId = project
        return swimlanesResult
    }
}
