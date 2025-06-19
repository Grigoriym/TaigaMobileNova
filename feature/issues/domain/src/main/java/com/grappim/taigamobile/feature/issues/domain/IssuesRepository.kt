package com.grappim.taigamobile.feature.issues.domain

import androidx.paging.PagingData
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.FiltersData
import kotlinx.coroutines.flow.Flow

interface IssuesRepository {
    fun getIssues(filtersData: FiltersData): Flow<PagingData<CommonTask>>
}
