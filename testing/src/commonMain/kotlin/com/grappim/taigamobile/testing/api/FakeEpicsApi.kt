package com.grappim.taigamobile.testing.api

import com.grappim.taigamobile.feature.epics.data.EpicsApi
import com.grappim.taigamobile.feature.epics.dto.LinkToEpicRequestDTO
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO

class FakeEpicsApi : EpicsApi {
    data class LinkToEpicCall(val epicId: Long, val request: LinkToEpicRequestDTO)
    data class UnlinkFromEpicCall(val epicId: Long, val userStoryId: Long)

    val linkCalls = mutableListOf<LinkToEpicCall>()
    val unlinkCalls = mutableListOf<UnlinkFromEpicCall>()

    override suspend fun getEpics(
        page: Int?,
        pageSize: Int,
        project: Long?,
        query: String?,
        assignedId: Long?,
        isClosed: Boolean?,
        watcherId: Long?,
        assignedIds: String?,
        ownerIds: String?,
        statuses: String?,
        tags: String?
    ) = emptyList<WorkItemResponseDTO>()

    override suspend fun linkToEpic(epicId: Long, linkToEpicRequest: LinkToEpicRequestDTO) {
        linkCalls += LinkToEpicCall(epicId, linkToEpicRequest)
    }

    override suspend fun unlinkFromEpic(epicId: Long, userStoryId: Long) {
        unlinkCalls += UnlinkFromEpicCall(epicId, userStoryId)
    }
}
