package com.grappim.taigamobile.testing.api

import com.grappim.taigamobile.feature.issues.data.IssuesApi
import com.grappim.taigamobile.feature.issues.dto.CreateIssueRequestDTO
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO

class FakeIssuesApi : IssuesApi {

    var createIssueResponse: WorkItemResponseDTO? = null
    var lastCreateIssueRequest: CreateIssueRequestDTO? = null

    override suspend fun getIssues(
        page: Int?,
        project: Long?,
        query: String?,
        sprint: Long?,
        isClosed: Boolean?,
        watcherId: Long?,
        pageSize: Int,
        assignedIds: String?,
        ownerIds: String?,
        priorities: String?,
        severities: String?,
        types: String?,
        roles: String?,
        statuses: String?,
        tags: String?
    ): List<WorkItemResponseDTO> = error("not used in this test")

    override suspend fun createIssue(createIssueRequest: CreateIssueRequestDTO): WorkItemResponseDTO {
        lastCreateIssueRequest = createIssueRequest
        return createIssueResponse ?: error("createIssueResponse not set")
    }
}
