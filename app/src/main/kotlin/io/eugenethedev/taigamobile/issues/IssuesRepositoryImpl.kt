package io.eugenethedev.taigamobile.issues

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import io.eugenethedev.taigamobile.domain.entities.CommonTask
import io.eugenethedev.taigamobile.domain.entities.FiltersData
import io.eugenethedev.taigamobile.state.Session
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IssuesRepositoryImpl @Inject constructor(
    private val issuesApi: IssuesApi,
    private val session: Session
) : IssuesRepository {
    override fun getIssues(filtersData: FiltersData): Flow<PagingData<CommonTask>> =
        Pager(
            PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            )
        ) {
            IssuesPagingSource(issuesApi, filtersData, session)
        }.flow
}