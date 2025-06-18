package io.eugenethedev.taigamobile.issues

import androidx.paging.PagingData
import io.eugenethedev.taigamobile.domain.entities.CommonTask
import io.eugenethedev.taigamobile.domain.entities.FiltersData
import kotlinx.coroutines.flow.Flow

interface IssuesRepository {
    fun getIssues(filtersData: FiltersData): Flow<PagingData<CommonTask>>
}