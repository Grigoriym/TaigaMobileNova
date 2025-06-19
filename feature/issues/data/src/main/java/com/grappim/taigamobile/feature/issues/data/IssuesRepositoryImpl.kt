package com.grappim.taigamobile.feature.issues.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.FiltersData
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IssuesRepositoryImpl @Inject constructor(
    private val issuesApi: IssuesApi,
    private val session: Session
) : IssuesRepository {
    override fun getIssues(filtersData: FiltersData): Flow<PagingData<CommonTask>> = Pager(
        PagingConfig(
            pageSize = 20,
            enablePlaceholders = false
        )
    ) {
        IssuesPagingSource(issuesApi, filtersData, session)
    }.flow
}
