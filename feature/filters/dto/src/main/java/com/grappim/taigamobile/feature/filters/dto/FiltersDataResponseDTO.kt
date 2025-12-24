package com.grappim.taigamobile.feature.filters.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FiltersDataResponseDTO(
    val statuses: List<Filter>,
    val tags: List<TagDTO>?,
    val roles: List<Filter>?,
    @SerialName(value = "assigned_to")
    val assignedTo: List<UserFilter>,
    val owners: List<UserFilter>,

    // user story filters
    val epics: List<EpicsFilter>?,

    // issue filters
    val priorities: List<Filter>?,
    val severities: List<Filter>?,
    val types: List<Filter>?
) {
    @Serializable
    data class Filter(val id: Long, val name: String, val color: String?, val count: Long, val order: Long)

    @Serializable
    data class UserFilter(
        val id: Long?,
        @SerialName(value = "full_name")
        val fullName: String,
        val count: Long
    )

    @Serializable
    data class EpicsFilter(val id: Long?, val ref: Long?, val subject: String?, val count: Long)
}

@Serializable
data class TagDTO(val color: String?, val count: Long, val name: String)
