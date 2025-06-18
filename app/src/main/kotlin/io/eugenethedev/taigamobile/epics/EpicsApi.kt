package io.eugenethedev.taigamobile.epics

import io.eugenethedev.taigamobile.data.api.CommonTaskResponse
import io.eugenethedev.taigamobile.domain.paging.CommonPagingSource
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface EpicsApi {
    @GET("epics")
    suspend fun getEpics(
        @Query("page") page: Int? = null,
        @Query("project") project: Long? = null,
        @Query("q") query: String? = null,
        @Query("assigned_to") assignedId: Long? = null,
        @Query("status__is_closed") isClosed: Boolean? = null,
        @Query("watchers") watcherId: Long? = null,
        @Query("page_size") pageSize: Int = CommonPagingSource.PAGE_SIZE,

        // List<Long?>?
        @Query("assigned_to", encoded = true) assignedIds: String? = null,

        // List<Long>?
        @Query("owner", encoded = true) ownerIds: String? = null,
        @Query("status", encoded = true) statuses: String? = null,

        // List<String>?
        @Query("tags", encoded = true) tags: String? = null,

        @Header("x-disable-pagination") disablePagination: Boolean? = (page == null).takeIf { it }
    ): List<CommonTaskResponse>
}
