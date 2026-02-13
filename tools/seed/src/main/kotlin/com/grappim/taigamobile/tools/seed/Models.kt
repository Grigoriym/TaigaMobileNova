package com.grappim.taigamobile.tools.seed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// region Auth

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    val type: String = "normal"
)

@Serializable
data class LoginResponse(
    val id: Int,
    @SerialName("auth_token") val authToken: String
)

// endregion

// region Project

@Serializable
data class CreateProjectRequest(
    val name: String,
    val description: String,
    @SerialName("is_private") val isPrivate: Boolean = false,
    @SerialName("is_backlog_activated") val isBacklogActivated: Boolean = true,
    @SerialName("is_kanban_activated") val isKanbanActivated: Boolean = true,
    @SerialName("is_issues_activated") val isIssuesActivated: Boolean = true,
    @SerialName("is_wiki_activated") val isWikiActivated: Boolean = true,
    @SerialName("is_epics_activated") val isEpicsActivated: Boolean = true
)

@Serializable
data class CreateProjectResponse(
    val id: Int,
    val name: String,
    val slug: String
)

@Serializable
data class ProjectDetailResponse(
    val id: Int,
    val name: String,
    @SerialName("epic_statuses") val epicStatuses: List<StatusItem>,
    @SerialName("us_statuses") val usStatuses: List<StatusItem>,
    @SerialName("task_statuses") val taskStatuses: List<StatusItem>,
    @SerialName("issue_statuses") val issueStatuses: List<StatusItem>,
    @SerialName("issue_types") val issueTypes: List<TypeItem>,
    val priorities: List<TypeItem>,
    val severities: List<TypeItem>,
    val points: List<PointItem>,
    val roles: List<RoleItem>
)

@Serializable
data class StatusItem(
    val id: Int,
    val name: String,
    @SerialName("is_closed") val isClosed: Boolean
)

@Serializable
data class TypeItem(
    val id: Int,
    val name: String
)

@Serializable
data class PointItem(
    val id: Int,
    val name: String,
    val value: Double?
)

@Serializable
data class RoleItem(
    val id: Int,
    val name: String,
    val computable: Boolean
)


// endregion

// region Milestone

@Serializable
data class CreateMilestoneRequest(
    val project: Int,
    val name: String,
    @SerialName("estimated_start") val estimatedStart: String,
    @SerialName("estimated_finish") val estimatedFinish: String,
    val order: Int = 1
)

@Serializable
data class MilestoneResponse(
    val id: Int,
    val name: String
)

// endregion

// region Epic

@Serializable
data class CreateEpicRequest(
    val project: Int,
    val subject: String,
    val description: String = "",
    val status: Int? = null,
    val color: String? = null,
    val tags: List<String> = emptyList()
)

@Serializable
data class EpicResponse(
    val id: Int,
    val ref: Int,
    val subject: String,
    val status: Int
)

@Serializable
data class EpicRelatedUserStoryRequest(
    val epic: Int,
    @SerialName("user_story") val userStory: Int
)

// endregion

// region User Story

@Serializable
data class CreateUserStoryRequest(
    val project: Int,
    val subject: String,
    val description: String = "",
    val status: Int? = null,
    val milestone: Int? = null,
    val tags: List<String> = emptyList(),
    val points: Map<String, Int>? = null
)

@Serializable
data class UserStoryResponse(
    val id: Int,
    val ref: Int,
    val subject: String,
    val status: Int,
    val milestone: Int?
)

// endregion

// region Task

@Serializable
data class CreateTaskRequest(
    val project: Int,
    val subject: String,
    val description: String = "",
    val status: Int? = null,
    @SerialName("user_story") val userStory: Int? = null,
    val milestone: Int? = null,
    val tags: List<String> = emptyList()
)

@Serializable
data class TaskResponse(
    val id: Int,
    val ref: Int,
    val subject: String,
    val status: Int
)

// endregion

// region Issue

@Serializable
data class CreateIssueRequest(
    val project: Int,
    val subject: String,
    val description: String = "",
    val status: Int? = null,
    val type: Int? = null,
    val severity: Int? = null,
    val priority: Int? = null,
    val tags: List<String> = emptyList()
)

@Serializable
data class IssueResponse(
    val id: Int,
    val ref: Int,
    val subject: String,
    val status: Int
)

// endregion

// region Wiki

@Serializable
data class CreateWikiPageRequest(
    val project: Int,
    val slug: String,
    val content: String = ""
)

@Serializable
data class WikiPageResponse(
    val id: Int,
    val slug: String
)

@Serializable
data class CreateWikiLinkRequest(
    val project: Int,
    val title: String,
    val href: String,
    val order: Long
)

@Serializable
data class WikiLinkResponse(
    val id: Int,
    val title: String,
    val href: String
)

// endregion
