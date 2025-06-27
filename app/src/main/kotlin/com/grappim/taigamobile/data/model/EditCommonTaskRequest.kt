package com.grappim.taigamobile.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate

@JsonClass(generateAdapter = true)
data class EditCommonTaskRequest(
    val subject: String,
    val description: String,
    val status: Long,
    val type: Long?,
    val severity: Long?,
    val priority: Long?,
    val milestone: Long?,
    @Json(name = "assigned_to") val assignedTo: Long?,
    @Json(name = "assigned_users") val assignedUsers: List<Long>,
    val watchers: List<Long>,
    val swimlane: Long?,
    @Json(name = "due_date") val dueDate: LocalDate?,
    val color: String?,
    val tags: List<List<String>>,
    @Json(name = "blocked_note") val blockedNote: String,
    @Json(name = "is_blocked") val isBlocked: Boolean,
    val version: Int
)
