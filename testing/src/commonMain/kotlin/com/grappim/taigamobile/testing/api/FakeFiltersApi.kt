package com.grappim.taigamobile.testing.api

import com.grappim.taigamobile.feature.filters.data.FiltersApi
import com.grappim.taigamobile.feature.filters.dto.FiltersDataResponseDTO
import com.grappim.taigamobile.testing.models.getFiltersDataResponseDTO

data class FakeFiltersApiCall(val taskPath: String, val project: Long, val milestone: Any?)

class FakeFiltersApi : FiltersApi {

    val calls = mutableListOf<FakeFiltersApiCall>()

    var response: FiltersDataResponseDTO = getFiltersDataResponseDTO()

    override suspend fun getCommonTaskFiltersData(
        taskPath: String,
        project: Long,
        milestone: Any?
    ): FiltersDataResponseDTO {
        calls += FakeFiltersApiCall(taskPath, project, milestone)
        return response
    }
}
