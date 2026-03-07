package com.grappim.taigamobile.testing.api

import com.grappim.taigamobile.feature.issues.data.IssuesApi
import com.grappim.taigamobile.feature.issues.dto.CreateIssueRequestDTO
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO

class FakeIssuesApi : IssuesApi {

    var createIssueResponse: WorkItemResponseDTO? = null
    var lastCreateIssueRequest: CreateIssueRequestDTO? = null

    override suspend fun createIssue(createIssueRequest: CreateIssueRequestDTO): WorkItemResponseDTO {
        lastCreateIssueRequest = createIssueRequest
        return createIssueResponse ?: error("createIssueResponse not set")
    }
}
