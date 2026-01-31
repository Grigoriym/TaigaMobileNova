package com.grappim.taigamobile.feature.userstories.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BulkUpdateKanbanOrderRequest(
    @SerialName("project_id")
    val projectId: Long,
    @SerialName("status_id")
    val statusId: Long,
    @SerialName("bulk_userstories")
    val bulkUserstories: List<Long>,
    @SerialName("swimlane_id")
    val swimlaneId: Long? = null,
    @SerialName("after_userstory_id")
    val afterUserstoryId: Long? = null,
    @SerialName("before_userstory_id")
    val beforeUserstoryId: Long? = null
)
