package com.grappim.taigamobile.feature.projects.domain

enum class ProjectValueType(
    val endpoint: String,
    val bulkKey: String?,
    val hasColor: Boolean = true,
    val hasClosed: Boolean = false,
    val hasArchived: Boolean = false,
    val hasValue: Boolean = false,
    val hasDaysToDue: Boolean = false,
    val deleteRequiresMoveTo: Boolean = true
) {
    EPIC_STATUSES("epic-statuses", "bulk_epic_statuses", hasClosed = true),
    US_STATUSES("userstory-statuses", "bulk_userstory_statuses", hasClosed = true, hasArchived = true),
    TASK_STATUSES("task-statuses", "bulk_task_statuses", hasClosed = true),
    ISSUE_STATUSES("issue-statuses", "bulk_issue_statuses", hasClosed = true),
    ISSUE_TYPES("issue-types", "bulk_issue_types"),
    PRIORITIES("priorities", "bulk_priorities"),
    SEVERITIES("severities", "bulk_severities"),
    POINTS("points", "bulk_points", hasColor = false, hasValue = true),
    US_DUE_DATES("userstory-due-dates", null, hasDaysToDue = true, deleteRequiresMoveTo = false),
    TASK_DUE_DATES("task-due-dates", null, hasDaysToDue = true, deleteRequiresMoveTo = false),
    ISSUE_DUE_DATES("issue-due-dates", null, hasDaysToDue = true, deleteRequiresMoveTo = false),
}
