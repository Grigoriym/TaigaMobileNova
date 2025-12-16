package com.grappim.taigamobile.feature.workitem.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class DueDateStatusDTO {
    @Json(name = "not_set")
    NotSet,

    @Json(name = "set")
    Set,

    @Json(name = "due_soon")
    DueSoon,

    @Json(name = "past_due")
    PastDue,

    @Json(name = "no_longer_applicable")
    NoLongerApplicable
}
