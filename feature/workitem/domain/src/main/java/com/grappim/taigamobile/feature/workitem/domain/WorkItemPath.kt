package com.grappim.taigamobile.feature.workitem.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier

@JvmInline
value class WorkItemPathPlural private constructor(val path: String) {
    constructor(commonTaskType: CommonTaskType) : this(
        when (commonTaskType) {
            CommonTaskType.UserStory -> "userstories"
            CommonTaskType.Task -> "tasks"
            CommonTaskType.Epic -> "epics"
            CommonTaskType.Issue -> "issues"
        }
    )
}

fun CommonTaskType.getPluralPath(): String = when (this) {
    CommonTaskType.UserStory -> "userstories"
    CommonTaskType.Task -> "tasks"
    CommonTaskType.Epic -> "epics"
    CommonTaskType.Issue -> "issues"
}

// singular form
@JvmInline
value class WorkItemPathSingular private constructor(val path: String) {
    constructor(commonTaskType: CommonTaskType) : this(
        when (commonTaskType) {
            CommonTaskType.UserStory -> "userstory"
            CommonTaskType.Task -> "task"
            CommonTaskType.Epic -> "epic"
            CommonTaskType.Issue -> "issue"
        }
    )
}

fun TaskIdentifier.getPluralPath(): String = when (this) {
    TaskIdentifier.Wiki -> "wiki"
    is TaskIdentifier.WorkItem -> this.commonTaskType.getPluralPath()
}
