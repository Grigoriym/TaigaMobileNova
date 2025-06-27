package com.grappim.taigamobile.feature.filters.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FiltersDataResponse(
    val statuses: List<Filter>,
    val tags: List<Filter>?,
    val roles: List<Filter>?,
    @Json(name = "assigned_to")
    val assignedTo: List<UserFilter>,
    val owners: List<UserFilter>,

    // user story filters
    val epics: List<EpicsFilter>?,

    // issue filters
    val priorities: List<Filter>?,
    val severities: List<Filter>?,
    val types: List<Filter>?
) {
    @JsonClass(generateAdapter = true)
    data class Filter(val id: Long?, val name: String?, val color: String?, val count: Int)

    @JsonClass(generateAdapter = true)
    data class UserFilter(
        val id: Long?,
        @Json(name = "full_name")
        val fullName: String,
        val count: Int
    )

    @JsonClass(generateAdapter = true)
    data class EpicsFilter(val id: Long?, val ref: Int?, val subject: String?, val count: Int)
}
