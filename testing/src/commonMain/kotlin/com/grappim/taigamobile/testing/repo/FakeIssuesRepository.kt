package com.grappim.taigamobile.testing.repo

import androidx.paging.PagingData
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.issues.domain.Issue
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeIssuesRepository : IssuesRepository {

    var getIssueResult: Issue? = null
    var getIssueThrows: Throwable? = null

    override suspend fun getIssue(id: Long, filtersData: FiltersData): Issue {
        getIssueThrows?.let { throw it }
        return getIssueResult ?: error("getIssueResult not set")
    }

    override fun getIssuesPaging(filtersData: FiltersData, query: String): Flow<PagingData<WorkItem>> =
        emptyFlow()

    override suspend fun createIssue(title: String, description: String, sprintId: Long?): WorkItem =
        error("not used in this test")
}
