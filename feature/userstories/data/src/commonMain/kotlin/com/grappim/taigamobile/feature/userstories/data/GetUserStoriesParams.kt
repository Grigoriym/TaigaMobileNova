package com.grappim.taigamobile.feature.userstories.data

data class GetUserStoriesParams(
    val project: Long? = null,
    val sprint: Any? = null,
    val status: Long? = null,
    val epic: Long? = null,
    val page: Int? = null,
    val assignedId: Long? = null,
    val isClosed: Boolean? = null,
    val watcherId: Long? = null,
    val isDashboard: Boolean? = null,
    val query: String? = null,
    val pageSize: Int = 20,
    val assignedIds: String? = null,
    val epics: String? = null,
    val ownerIds: String? = null,
    val roles: String? = null,
    val statuses: String? = null,
    val tags: String? = null
)
