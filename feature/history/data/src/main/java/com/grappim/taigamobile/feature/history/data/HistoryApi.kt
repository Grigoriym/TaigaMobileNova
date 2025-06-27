package com.grappim.taigamobile.feature.history.data

import com.grappim.taigamobile.core.domain.Comment
import com.grappim.taigamobile.core.domain.CommonTaskPathSingular
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HistoryApi {
    @GET("history/{taskPath}/{id}?type=comment")
    suspend fun getCommonTaskComments(
        @Path("taskPath") taskPath: CommonTaskPathSingular,
        @Path("id") id: Long
    ): List<Comment>

    @POST("history/{taskPath}/{id}/delete_comment")
    suspend fun deleteCommonTaskComment(
        @Path("taskPath") taskPath: CommonTaskPathSingular,
        @Path("id") id: Long,
        @Query("id") commentId: String
    )
}
