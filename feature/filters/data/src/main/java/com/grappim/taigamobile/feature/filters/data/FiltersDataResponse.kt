package com.grappim.taigamobile.feature.filters.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FiltersDataResponse(
    val statuses: List<Filter>,
    val tags: List<TagDTO>?,
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
    data class Filter(val id: Long, val name: String, val color: String?, val count: Long, val order: Long)

    @JsonClass(generateAdapter = true)
    data class UserFilter(
        val id: Long?,
        @Json(name = "full_name")
        val fullName: String,
        val count: Long
    )

    @JsonClass(generateAdapter = true)
    data class EpicsFilter(val id: Long?, val ref: Long?, val subject: String?, val count: Long)
}

@JsonClass(generateAdapter = true)
data class TagDTO(val color: String?, val count: Long, val name: String)
