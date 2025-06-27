package com.grappim.taigamobile.feature.filters.data

import com.grappim.taigamobile.core.domain.CommonTaskPathPlural
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FiltersApi {
    @GET("{taskPath}/filters_data")
    suspend fun getCommonTaskFiltersData(
        @Path("taskPath") taskPath: CommonTaskPathPlural,
        @Query("project") project: Long,
        @Query("milestone") milestone: Any? = null
    ): FiltersDataResponse
}
