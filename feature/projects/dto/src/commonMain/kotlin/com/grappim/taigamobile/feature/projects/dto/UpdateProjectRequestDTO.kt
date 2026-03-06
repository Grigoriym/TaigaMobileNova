package com.grappim.taigamobile.feature.projects.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateProjectRequestDTO(
    val name: String,
    val description: String,
    @SerialName("is_private") val isPrivate: Boolean,
    @SerialName("is_looking_for_people") val isLookingForPeople: Boolean,
    @SerialName("looking_for_people_note") val lookingForPeopleNote: String,
    @SerialName("is_contact_activated") val isContactActivated: Boolean
)
