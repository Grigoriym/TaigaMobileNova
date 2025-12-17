package com.grappim.taigamobile.core.domain

const val PATH_TO_USERSTORY = "us"
const val PATH_TO_TASK = "task"
const val PATH_TO_EPIC = "epic"
const val PATH_TO_ISSUE = "issue"

fun transformTaskTypeForCopyLink(commonTaskType: CommonTaskType) = when (commonTaskType) {
    CommonTaskType.UserStory -> PATH_TO_USERSTORY
    CommonTaskType.Task -> PATH_TO_TASK
    CommonTaskType.Epic -> PATH_TO_EPIC
    CommonTaskType.Issue -> PATH_TO_ISSUE
    CommonTaskType.Wiki -> "wiki"
}
