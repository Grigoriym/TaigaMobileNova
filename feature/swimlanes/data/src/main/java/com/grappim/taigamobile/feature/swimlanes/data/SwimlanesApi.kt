package com.grappim.taigamobile.feature.swimlanes.data

import com.grappim.taigamobile.core.domain.Swimlane
import retrofit2.http.GET
import retrofit2.http.Query

interface SwimlanesApi {
    @GET("swimlanes")
    suspend fun getSwimlanes(@Query("project") project: Long): List<Swimlane>
}
