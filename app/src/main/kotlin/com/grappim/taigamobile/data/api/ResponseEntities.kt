package com.grappim.taigamobile.data.api

import com.grappim.taigamobile.core.domain.CustomFieldType
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

@JsonClass(generateAdapter = true)
data class CustomAttributeResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val order: Int,
    val type: CustomFieldType,
    val extra: List<String>?
)

@JsonClass(generateAdapter = true)
data class CustomAttributesValuesResponse(
    @Json(name = "attributes_values")
    val attributesValues: Map<Long, Any?>,
    val version: Int
)
