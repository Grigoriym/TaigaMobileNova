package com.grappim.taigamobile.core.domain

/**
 * Since API endpoints for different types of tasks are often the same (only part in the path is different),
 * here are some value classes to simplify interactions with API
 */

// plural form
@JvmInline
value class CommonTaskPathPlural private constructor(val path: String) {
    constructor(commonTaskType: CommonTaskType) : this(
        when (commonTaskType) {
            CommonTaskType.UserStory -> "userstories"
            CommonTaskType.Task -> "tasks"
            CommonTaskType.Epic -> "epics"
            CommonTaskType.Issue -> "issues"
        }
    )
}

// singular form
@JvmInline
value class CommonTaskPathSingular private constructor(val path: String) {
    constructor(commonTaskType: CommonTaskType) : this(
        when (commonTaskType) {
            CommonTaskType.UserStory -> "userstory"
            CommonTaskType.Task -> "task"
            CommonTaskType.Epic -> "epic"
            CommonTaskType.Issue -> "issue"
        }
    )
}

fun StatusesFilter.toStatus(statusType: StatusType) = Status(
    id = id,
    name = name,
    color = color,
    type = statusType
)

const val PATH_TO_USERSTORY = "us"
const val PATH_TO_TASK = "task"
const val PATH_TO_EPIC = "epic"
const val PATH_TO_ISSUE = "issue"

fun transformTaskTypeForCopyLink(commonTaskType: CommonTaskType) = when (commonTaskType) {
    CommonTaskType.UserStory -> PATH_TO_USERSTORY
    CommonTaskType.Task -> PATH_TO_TASK
    CommonTaskType.Epic -> PATH_TO_EPIC
    CommonTaskType.Issue -> PATH_TO_ISSUE
}
