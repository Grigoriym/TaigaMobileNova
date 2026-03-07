package com.grappim.taigamobile.feature.swimlanes.data

import kotlinx.serialization.Serializable

@Serializable
data class SwimlaneDTO(val id: Long, val name: String, val order: Long)
