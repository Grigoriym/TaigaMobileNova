package com.grappim.taigamobile.feature.workitem.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DueDateStatusDTO {
    @SerialName(value = "not_set")
    NotSet,

    @SerialName(value = "set")
    Set,

    @SerialName(value = "due_soon")
    DueSoon,

    @SerialName(value = "past_due")
    PastDue,

    @SerialName(value = "no_longer_applicable")
    NoLongerApplicable
}
