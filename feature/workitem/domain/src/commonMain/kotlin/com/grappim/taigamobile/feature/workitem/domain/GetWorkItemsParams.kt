package com.grappim.taigamobile.feature.workitem.domain

data class GetWorkItemsParams(
    val assignedId: Long? = null,
    val isClosed: Boolean? = null,
    val watcherId: Long? = null,
    val isDashboard: Boolean? = null,
    val assignedIds: String? = null,
    val isBlocked: Boolean? = null,
    val modifiedDateGte: String? = null,
    val finishDateGte: String? = null,
    val milestoneId: Long? = null,
    val pageSize: Int? = null
)
