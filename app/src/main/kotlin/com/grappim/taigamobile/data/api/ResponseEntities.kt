package com.grappim.taigamobile.data.api

import com.grappim.taigamobile.core.domain.CustomFieldType
import com.grappim.taigamobile.core.domain.DueDateStatus
import com.grappim.taigamobile.core.domain.EpicShortInfo
import com.grappim.taigamobile.core.domain.Project
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.domain.UserStoryShortInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class RefreshTokenResponse(
    @Json(name = "auth_token") val authToken: String,
    val refresh: String
)

@JsonClass(generateAdapter = true)
data class FiltersDataResponse(
    val statuses: List<Filter>,
    val tags: List<Filter>?,
    val roles: List<Filter>?,
    val assigned_to: List<UserFilter>,
    val owners: List<UserFilter>,

    // user story filters
    val epics: List<EpicsFilter>?,

    // issue filters
    val priorities: List<Filter>?,
    val severities: List<Filter>?,
    val types: List<Filter>?
) {
    @JsonClass(generateAdapter = true)
    data class Filter(
        val id: Long?,
        val name: String?,
        val color: String?,
        val count: Int
    )

    @JsonClass(generateAdapter = true)
    data class UserFilter(
        val id: Long?,
        val full_name: String,
        val count: Int
    )

    @JsonClass(generateAdapter = true)
    data class EpicsFilter(
        val id: Long?,
        val ref: Int?,
        val subject: String?,
        val count: Int
    )
}

@JsonClass(generateAdapter = true)
data class MemberStatsResponse(
    val closed_bugs: Map<String, Int>, // because api returns "null" key along with id keys, so...
    val closed_tasks: Map<String, Int>,
    val created_bugs: Map<String, Int>,
    val iocaine_tasks: Map<String, Int>,
    val wiki_changes: Map<String, Int>
)

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
    val attributes_values: Map<Long, Any?>,
    val version: Int
)