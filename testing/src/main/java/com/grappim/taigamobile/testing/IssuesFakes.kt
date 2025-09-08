package com.grappim.taigamobile.testing

import com.grappim.taigamobile.core.domain.Comment
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.CustomField
import com.grappim.taigamobile.core.domain.CustomFieldType
import com.grappim.taigamobile.core.domain.CustomFieldValue
import com.grappim.taigamobile.core.domain.CustomFields
import com.grappim.taigamobile.core.domain.DueDateStatus
import com.grappim.taigamobile.feature.filters.domain.model.Priority
import com.grappim.taigamobile.feature.filters.domain.model.Severity
import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.filters.domain.model.Type
import com.grappim.taigamobile.feature.issues.domain.IssueDetailsData
import com.grappim.taigamobile.feature.issues.domain.IssueTask
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate
import java.time.LocalDateTime

fun getIssueDetailsData(): IssueDetailsData = IssueDetailsData(
    issueTask = getIssueTask(),
    attachments = persistentListOf(
        getAttachment(),
        getAttachment()
    ),
    sprint = getSprint(),
    customFields = getCustomFields(),
    comments = persistentListOf(
        getComment(),
        getComment()
    ),
    creator = getUser(),
    assignees = persistentListOf(
        getUser(),
        getUser()
    ),
    watchers = persistentListOf(
        getUser(),
        getUser()
    ),
    isAssignedToMe = getRandomBoolean(),
    isWatchedByMe = getRandomBoolean(),
    filtersData = getFiltersData()
)

fun getIssueTask(): IssueTask = IssueTask(
    id = getRandomLong(),
    version = getRandomLong(),
    ref = getRandomInt(),
    creatorId = getRandomLong(),
    title = getRandomString(),
    description = getRandomString(),
    createdDateTime = LocalDateTime.now(),
    dueDate = LocalDate.now().plusDays(getRandomLong() % 30),
    dueDateStatus = DueDateStatus.Set,
    project = getProject(),
    taskType = CommonTaskType.Issue,
    isClosed = getRandomBoolean(),
    tags = persistentListOf(getTag()),
    colors = listOf(getRandomString()),
    blockedNote = getRandomString(),
    assignee = getUser(),
    assignedUserIds = listOf(getRandomLong()),
    watcherUserIds = listOf(getRandomLong()),
    milestone = getRandomLong(),
    copyLinkUrl = getRandomString(),
    status = getStatus(),
    type = getType(),
    priority = getPriority(),
    severity = getSeverity()
)

fun getTag(): Tag = Tag(
    color = getRandomString(),
    count = getRandomLong(),
    name = getRandomString()
)

fun getStatus(): Status = Status(
    color = getRandomString(),
    id = getRandomLong(),
    name = getRandomString(),
    count = getRandomLong(),
    order = getRandomLong()
)

fun getType(): Type = Type(
    color = getRandomString(),
    id = getRandomLong(),
    name = getRandomString(),
    count = getRandomLong(),
    order = getRandomLong()
)

fun getPriority(): Priority = Priority(
    color = getRandomString(),
    id = getRandomLong(),
    name = getRandomString(),
    count = getRandomLong(),
    order = getRandomLong()
)

fun getSeverity(): Severity = Severity(
    color = getRandomString(),
    id = getRandomLong(),
    name = getRandomString(),
    count = getRandomLong(),
    order = getRandomLong()
)

fun getCustomFields(): CustomFields = CustomFields(
    fields = listOf(
        getCustomField(),
        getCustomField()
    ),
    version = getRandomLong()
)

fun getCustomField(): CustomField = CustomField(
    id = getRandomLong(),
    type = CustomFieldType.Text,
    name = getRandomString(),
    description = getRandomString(),
    value = getCustomFieldValue(),
    options = listOf(
        getRandomString(),
        getRandomString()
    )
)

fun getCustomFieldValue(): CustomFieldValue = CustomFieldValue(
    value = getRandomString(),
)

fun getComment(): Comment = Comment(
    id = getRandomString(),
    author = getUserDTO(),
    text = getRandomString(),
    postDateTime = LocalDateTime.now(),
    deleteDate = LocalDateTime.now(),
    canDelete = getRandomBoolean()
)
