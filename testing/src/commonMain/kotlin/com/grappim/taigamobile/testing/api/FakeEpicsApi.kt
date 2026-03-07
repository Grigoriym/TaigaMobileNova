package com.grappim.taigamobile.testing.api

import com.grappim.taigamobile.feature.epics.data.EpicsApi
import com.grappim.taigamobile.feature.epics.dto.LinkToEpicRequestDTO

class FakeEpicsApi : EpicsApi {
    data class LinkToEpicCall(val epicId: Long, val request: LinkToEpicRequestDTO)
    data class UnlinkFromEpicCall(val epicId: Long, val userStoryId: Long)

    val linkCalls = mutableListOf<LinkToEpicCall>()
    val unlinkCalls = mutableListOf<UnlinkFromEpicCall>()

    override suspend fun linkToEpic(epicId: Long, linkToEpicRequest: LinkToEpicRequestDTO) {
        linkCalls += LinkToEpicCall(epicId, linkToEpicRequest)
    }

    override suspend fun unlinkFromEpic(epicId: Long, userStoryId: Long) {
        unlinkCalls += UnlinkFromEpicCall(epicId, userStoryId)
    }
}
