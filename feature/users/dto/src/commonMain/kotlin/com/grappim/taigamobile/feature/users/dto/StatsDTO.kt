package com.grappim.taigamobile.feature.users.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatsDTO(
    val roles: List<String> = emptyList(),
    @SerialName(value = "total_num_closed_userstories")
    val totalNumClosedUserStories: Int,
    @SerialName(value = "total_num_contacts")
    val totalNumContacts: Int,
    @SerialName(value = "total_num_projects")
    val totalNumProjects: Int
)
