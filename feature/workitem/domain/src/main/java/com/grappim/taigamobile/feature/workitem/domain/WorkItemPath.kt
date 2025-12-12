package com.grappim.taigamobile.feature.workitem.domain

import com.grappim.taigamobile.core.domain.CommonTaskType

@JvmInline
value class WorkItemPathPlural private constructor(val path: String) {
    constructor(commonTaskType: CommonTaskType) : this(
        when (commonTaskType) {
            CommonTaskType.UserStory -> "userstories"
            CommonTaskType.Task -> "tasks"
            CommonTaskType.Epic -> "epics"
            CommonTaskType.Issue -> "issues"
            CommonTaskType.Wiki -> "wiki"
        }
    )
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
            CommonTaskType.Wiki -> "wiki"
        }
    )
}
