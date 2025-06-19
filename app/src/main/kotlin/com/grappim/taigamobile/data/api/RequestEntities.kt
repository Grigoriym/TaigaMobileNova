package com.grappim.taigamobile.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate

@JsonClass(generateAdapter = true)
data class RefreshTokenRequest(
    val refresh: String
)

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

@JsonClass(generateAdapter = true)
data class CreateCommentRequest(
    val comment: String,
    val version: Int
)

@JsonClass(generateAdapter = true)
data class CreateCommonTaskRequest(
    val project: Long,
    val subject: String,
    val description: String,
    val status: Long?
)

@JsonClass(generateAdapter = true)
data class CreateTaskRequest(
    val project: Long,
    val subject: String,
    val description: String,
    val milestone: Long?,
    val user_story: Long?
)

@JsonClass(generateAdapter = true)
data class CreateIssueRequest(
    val project: Long,
    val subject: String,
    val description: String,
    val milestone: Long?,
)

@JsonClass(generateAdapter = true)
data class CreateUserStoryRequest(
    val project: Long,
    val subject: String,
    val description: String,
    val status: Long?,
    val swimlane: Long?
)

@JsonClass(generateAdapter = true)
data class LinkToEpicRequest(
    val epic: String,
    val user_story: Long
)

@JsonClass(generateAdapter = true)
data class PromoteToUserStoryRequest(
    val project_id: Long
)

@JsonClass(generateAdapter = true)
data class EditCustomAttributesValuesRequest(
    val attributes_values: Map<Long, Any?>,
    val version: Int
)
