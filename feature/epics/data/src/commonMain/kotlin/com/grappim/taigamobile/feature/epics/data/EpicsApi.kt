package com.grappim.taigamobile.feature.epics.data

import com.grappim.taigamobile.feature.epics.dto.LinkToEpicRequestDTO
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.discardRemaining
import org.koin.core.annotation.Single

interface EpicsApi {
    suspend fun linkToEpic(epicId: Long, linkToEpicRequest: LinkToEpicRequestDTO)

    suspend fun unlinkFromEpic(epicId: Long, userStoryId: Long)
}

@Single(binds = [EpicsApi::class])
class EpicsApiImpl(private val httpClient: HttpClient) : EpicsApi {

    override suspend fun linkToEpic(epicId: Long, linkToEpicRequest: LinkToEpicRequestDTO) {
        httpClient.post("epics/$epicId/related_userstories") {
            setBody(linkToEpicRequest)
        }.discardRemaining()
    }

    override suspend fun unlinkFromEpic(epicId: Long, userStoryId: Long) {
        httpClient.delete("epics/$epicId/related_userstories/$userStoryId").discardRemaining()
    }
}
