package com.grappim.taigamobile.testing.repo

import androidx.paging.PagingData
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.issues.domain.Issue
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeIssuesRepository : IssuesRepository {

    var getIssueResult: Issue? = null
    var getIssueThrows: Throwable? = null

    override suspend fun getIssue(id: Long, filtersData: FiltersData): Issue {
        getIssueThrows?.let { throw it }
        return getIssueResult ?: error("getIssueResult not set")
    }

    var getIssuesPagingResult: ImmutableList<WorkItem> = persistentListOf()

    override fun getIssuesPaging(filtersData: FiltersData, query: String): Flow<PagingData<WorkItem>> =
        if (getIssuesPagingResult.isEmpty()) {
            flowOf(PagingData.empty())
        } else {
            flowOf(PagingData.from(getIssuesPagingResult))
        }

    data class CreateIssueCall(val title: String, val description: String, val sprintId: Long?)

    var createIssueResult: WorkItem? = null
    var createIssueThrows: Throwable? = null
    val createIssueCalls: MutableList<CreateIssueCall> = mutableListOf()

    override suspend fun createIssue(title: String, description: String, sprintId: Long?): WorkItem {
        createIssueCalls += CreateIssueCall(
            title = title,
            description = description,
            sprintId = sprintId
        )
        createIssueThrows?.let { throw it }
        return createIssueResult ?: error("createIssueResult not set")
    }
}
