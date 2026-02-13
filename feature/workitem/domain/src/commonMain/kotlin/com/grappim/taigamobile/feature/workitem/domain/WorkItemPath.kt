package com.grappim.taigamobile.feature.workitem.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier

fun CommonTaskType.getPluralPath(): String = when (this) {
    CommonTaskType.UserStory -> "userstories"
    CommonTaskType.Task -> "tasks"
    CommonTaskType.Epic -> "epics"
    CommonTaskType.Issue -> "issues"
}

fun CommonTaskType.getSingularPath(): String = when (this) {
    CommonTaskType.UserStory -> "userstory"
    CommonTaskType.Task -> "task"
    CommonTaskType.Epic -> "epic"
    CommonTaskType.Issue -> "issue"
}

fun TaskIdentifier.getPluralPath(): String = when (this) {
    TaskIdentifier.Wiki -> "wiki"
    is TaskIdentifier.WorkItem -> this.commonTaskType.getPluralPath()
}
