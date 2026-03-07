package com.grappim.taigamobile.feature.issues.data

import com.grappim.taigamobile.feature.issues.dto.CreateIssueRequestDTO
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.koin.core.annotation.Single

interface IssuesApi {
    suspend fun createIssue(createIssueRequest: CreateIssueRequestDTO): WorkItemResponseDTO
}

@Single(binds = [IssuesApi::class])
class IssuesApiImpl(private val httpClient: HttpClient) : IssuesApi {
    override suspend fun createIssue(createIssueRequest: CreateIssueRequestDTO): WorkItemResponseDTO =
        httpClient.post("issues") {
            setBody(createIssueRequest)
        }.body()
}
