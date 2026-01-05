package com.grappim.taigamobile.feature.filters.data

import com.grappim.taigamobile.feature.filters.dto.FiltersDataResponseDTO
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FiltersApi {
    @GET("{taskPath}/filters_data")
    suspend fun getCommonTaskFiltersData(
        @Path("taskPath") taskPath: WorkItemPathPlural,
        @Query("project") project: Long,
        @Query("milestone") milestone: Any? = null
    ): FiltersDataResponseDTO
}
