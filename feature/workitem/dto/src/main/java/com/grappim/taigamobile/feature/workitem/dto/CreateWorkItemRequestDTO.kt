package com.grappim.taigamobile.feature.workitem.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateWorkItemRequestDTO(val project: Long, val subject: String, val description: String, val status: Long?)
