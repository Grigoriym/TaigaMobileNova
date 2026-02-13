package com.grappim.taigamobile.feature.swimlanes.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.koin.core.annotation.Single

@Single
class SwimlanesApi(private val httpClient: HttpClient) {

    suspend fun getSwimlanes(project: Long): List<SwimlaneDTO> =
        httpClient.get("swimlanes") {
            url { parameters.append("project", project.toString()) }
        }.body()
}
