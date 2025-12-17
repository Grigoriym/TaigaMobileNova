package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.filters.domain.model.Priority
import com.grappim.taigamobile.feature.filters.domain.model.Severity
import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.filters.domain.model.Type
import com.grappim.taigamobile.feature.issues.domain.Issue
import com.grappim.taigamobile.feature.issues.domain.IssueDetailsData
import com.grappim.taigamobile.feature.issues.ui.model.IssueUI
import com.grappim.taigamobile.feature.workitem.domain.Comment
import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomField
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFieldType
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFieldValue
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFields
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate
import java.time.LocalDateTime

fun getIssueDetailsData(): IssueDetailsData = IssueDetailsData(
    issue = getIssueTask(),
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

fun getIssueTask(): Issue = Issue(
    id = getRandomLong(),
    version = getRandomLong(),
    ref = getRandomLong(),
    creatorId = getRandomLong(),
    title = getRandomString(),
    description = getRandomString(),
    createdDateTime = LocalDateTime.now(),
    dueDate = LocalDate.now().plusDays(getRandomLong() % 30),
    dueDateStatus = DueDateStatus.Set,
    project = getProject(),
    isClosed = getRandomBoolean(),
    tags = persistentListOf(getTag()),
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

fun getIssueUI(): IssueUI = IssueUI(
    id = getRandomLong(),
    version = getRandomLong(),
    ref = getRandomLong(),
    creatorId = getRandomLong(),
    title = getRandomString(),
    description = getRandomString(),
    createdDateTime = LocalDateTime.now(),
    dueDate = LocalDate.now().plusDays(getRandomLong() % 30),
    dueDateStatus = DueDateStatus.Set,
    isClosed = getRandomBoolean(),
    blockedNote = getRandomString(),
    assignedUserIds = listOf(getRandomLong()),
    watcherUserIds = listOf(getRandomLong()),
    copyLinkUrl = getRandomString(),
    status = getStatusUI(),
    promotedUserStories = persistentListOf()
)

fun getTag(): Tag = Tag(
    color = getRandomString(),
    name = getRandomString()
)

fun getStatus(): Status = Status(
    color = getRandomString(),
    id = getRandomLong(),
    name = getRandomString()
)

fun getType(): Type = Type(
    color = getRandomString(),
    id = getRandomLong(),
    name = getRandomString()
)

fun getPriority(): Priority = Priority(
    color = getRandomString(),
    id = getRandomLong(),
    name = getRandomString()
)

fun getSeverity(): Severity = Severity(
    color = getRandomString(),
    id = getRandomLong(),
    name = getRandomString()
)

fun getCustomFields(): CustomFields = CustomFields(
    fields = persistentListOf(
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
    options = persistentListOf(
        getRandomString(),
        getRandomString()
    )
)

fun getCustomFieldValue(): CustomFieldValue = CustomFieldValue(
    value = getRandomString(),
)

fun getComment(): Comment = Comment(
    id = getRandomString(),
    author = getUser(),
    text = getRandomString(),
    postDateTime = LocalDateTime.now(),
    deleteDate = LocalDateTime.now(),
    canDelete = getRandomBoolean()
)
