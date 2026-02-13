package com.grappim.taigamobile.feature.userstories.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BulkUpdateKanbanOrderResponseItem(
    val id: Long,
    val status: Long,
    @SerialName("kanban_order")
    val kanbanOrder: Long,
    val swimlane: Long? = null
)
