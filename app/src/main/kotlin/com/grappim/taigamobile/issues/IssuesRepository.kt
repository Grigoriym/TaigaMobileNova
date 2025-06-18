package com.grappim.taigamobile.issues

import androidx.paging.PagingData
import com.grappim.taigamobile.domain.entities.CommonTask
import com.grappim.taigamobile.domain.entities.FiltersData
import kotlinx.coroutines.flow.Flow

interface IssuesRepository {
    fun getIssues(filtersData: FiltersData): Flow<PagingData<CommonTask>>
}