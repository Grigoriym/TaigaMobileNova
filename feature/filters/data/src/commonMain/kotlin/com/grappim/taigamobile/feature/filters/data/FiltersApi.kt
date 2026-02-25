package com.grappim.taigamobile.feature.filters.data

import com.grappim.taigamobile.feature.filters.dto.FiltersDataResponseDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.koin.core.annotation.Single

interface FiltersApi {

    suspend fun getCommonTaskFiltersData(
        taskPath: String,
        project: Long,
        milestone: Any? = null
    ): FiltersDataResponseDTO
}

@Single(binds = [FiltersApi::class])
class FiltersApiImpl(private val httpClient: HttpClient) : FiltersApi {
    override suspend fun getCommonTaskFiltersData(
        taskPath: String,
        project: Long,
        milestone: Any?
    ): FiltersDataResponseDTO = httpClient.get("$taskPath/filters_data") {
        url {
            parameters.append("project", project.toString())
            if (milestone != null) parameters.append("milestone", milestone.toString())
        }
    }.body()
}
