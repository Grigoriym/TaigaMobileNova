package com.grappim.taigamobile.data.repositories

import com.grappim.taigamobile.core.api.fixNullColor
import com.grappim.taigamobile.core.api.handle404
import com.grappim.taigamobile.core.api.toCommonTask
import com.grappim.taigamobile.core.api.withIO
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskExtended
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.CustomField
import com.grappim.taigamobile.core.domain.CustomFieldType
import com.grappim.taigamobile.core.domain.CustomFieldValue
import com.grappim.taigamobile.core.domain.CustomFields
import com.grappim.taigamobile.core.domain.EpicsFilter
import com.grappim.taigamobile.core.domain.FiltersData
import com.grappim.taigamobile.core.domain.RolesFilter
import com.grappim.taigamobile.core.domain.Status
import com.grappim.taigamobile.core.domain.StatusType
import com.grappim.taigamobile.core.domain.StatusesFilter
import com.grappim.taigamobile.core.domain.Swimlane
import com.grappim.taigamobile.core.domain.Tag
import com.grappim.taigamobile.core.domain.TagsFilter
import com.grappim.taigamobile.core.domain.TasksRepository
import com.grappim.taigamobile.core.domain.UsersFilter
import com.grappim.taigamobile.core.domain.commaString
import com.grappim.taigamobile.core.domain.tagsCommaString
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.data.api.CommonTaskPathPlural
import com.grappim.taigamobile.data.api.CommonTaskPathSingular
import com.grappim.taigamobile.data.api.CreateCommentRequest
import com.grappim.taigamobile.data.api.CreateCommonTaskRequest
import com.grappim.taigamobile.data.api.CreateIssueRequest
import com.grappim.taigamobile.data.api.CreateTaskRequest
import com.grappim.taigamobile.data.api.CreateUserStoryRequest
import com.grappim.taigamobile.data.api.EditCommonTaskRequest
import com.grappim.taigamobile.data.api.EditCustomAttributesValuesRequest
import com.grappim.taigamobile.data.api.LinkToEpicRequest
import com.grappim.taigamobile.data.api.PromoteToUserStoryRequest
import com.grappim.taigamobile.data.api.TaigaApi
import com.grappim.taigamobile.di.toLocalDate
import com.grappim.taigamobile.feature.epics.data.EpicsApi
import com.grappim.taigamobile.feature.issues.data.IssuesApi
import com.grappim.taigamobile.feature.sprint.data.SprintApi
import com.grappim.taigamobile.feature.sprint.data.toSprint
import com.grappim.taigamobile.feature.userstories.data.UserStoriesApi
import kotlinx.coroutines.async
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import java.time.LocalDate
import javax.inject.Inject

class TasksRepositoryImpl @Inject constructor(
    private val taigaApi: TaigaApi,
    private val session: Session,
    private val epicsApi: EpicsApi,
    private val userStoriesApi: UserStoriesApi,
    private val sprintApi: SprintApi,
    private val issuesApi: IssuesApi,
    private val serverStorage: ServerStorage
) : TasksRepository {
    private val currentProjectId get() = session.currentProject
    private val currentUserId get() = session.currentUserId.value

    private fun StatusesFilter.toStatus(statusType: StatusType) = Status(
        id = id,
        name = name,
        color = color,
        type = statusType
    )

    override suspend fun getFiltersData(
        commonTaskType: CommonTaskType,
        isCommonTaskFromBacklog: Boolean
    ) = taigaApi.getCommonTaskFiltersData(
        taskPath = CommonTaskPathPlural(commonTaskType),
        project = currentProjectId,
        milestone = if (isCommonTaskFromBacklog) "null" else null
    ).let {
        FiltersData(
            assignees = it.assignedTo.map {
                UsersFilter(
                    id = it.id,
                    name = it.fullName,
                    count = it.count
                )
            },
            roles = it.roles.orEmpty().map {
                RolesFilter(
                    id = it.id!!,
                    name = it.name!!,
                    count = it.count
                )
            },
            tags = it.tags.orEmpty().map {
                TagsFilter(
                    name = it.name!!,
                    color = it.color.fixNullColor(),
                    count = it.count
                )
            },
            statuses = it.statuses.map {
                StatusesFilter(
                    id = it.id!!,
                    color = it.color.fixNullColor(),
                    name = it.name!!,
                    count = it.count
                )
            },
            createdBy = it.owners.map {
                UsersFilter(
                    id = it.id!!,
                    name = it.fullName,
                    count = it.count
                )
            },
            priorities = it.priorities.orEmpty().map {
                StatusesFilter(
                    id = it.id!!,
                    color = it.color.fixNullColor(),
                    name = it.name!!,
                    count = it.count
                )
            },
            severities = it.severities.orEmpty().map {
                StatusesFilter(
                    id = it.id!!,
                    color = it.color.fixNullColor(),
                    name = it.name!!,
                    count = it.count
                )
            },
            types = it.types.orEmpty().map {
                StatusesFilter(
                    id = it.id!!,
                    color = it.color.fixNullColor(),
                    name = it.name!!,
                    count = it.count
                )
            },
            epics = it.epics.orEmpty().map {
                EpicsFilter(
                    id = it.id,
                    name = it.subject?.let { s -> "#${it.ref} $s" }.orEmpty(),
                    count = it.count
                )
            }
        )
    }

    override suspend fun getWorkingOn() = withIO {
        val epics = async {
            epicsApi.getEpics(assignedId = currentUserId, isClosed = false)
                .map { it.toCommonTask(CommonTaskType.Epic) }
        }

        val stories = async {
            userStoriesApi.getUserStories(
                assignedId = currentUserId,
                isClosed = false,
                isDashboard = true
            )
                .map { it.toCommonTask(CommonTaskType.UserStory) }
        }

        val tasks = async {
            taigaApi.getTasks(assignedId = currentUserId, isClosed = false)
                .map { it.toCommonTask(CommonTaskType.Task) }
        }

        val issues = async {
            issuesApi.getIssues(assignedIds = currentUserId.toString(), isClosed = false)
                .map { it.toCommonTask(CommonTaskType.Issue) }
        }

        epics.await() + stories.await() + tasks.await() + issues.await()
    }

    override suspend fun getWatching() = withIO {
        val epics = async {
            epicsApi.getEpics(watcherId = currentUserId, isClosed = false)
                .map { it.toCommonTask(CommonTaskType.Epic) }
        }

        val stories = async {
            userStoriesApi.getUserStories(
                watcherId = currentUserId,
                isClosed = false,
                isDashboard = true
            )
                .map { it.toCommonTask(CommonTaskType.UserStory) }
        }

        val tasks = async {
            taigaApi.getTasks(watcherId = currentUserId, isClosed = false)
                .map { it.toCommonTask(CommonTaskType.Task) }
        }

        val issues = async {
            issuesApi.getIssues(watcherId = currentUserId, isClosed = false)
                .map { it.toCommonTask(CommonTaskType.Issue) }
        }

        epics.await() + stories.await() + tasks.await() + issues.await()
    }

    override suspend fun getStatuses(commonTaskType: CommonTaskType) =
        getFiltersData(commonTaskType).statuses.map { it.toStatus(StatusType.Status) }

    override suspend fun getStatusByType(commonTaskType: CommonTaskType, statusType: StatusType) =
        withIO {
            if (commonTaskType != CommonTaskType.Issue && statusType != StatusType.Status) {
                throw UnsupportedOperationException("Cannot get $statusType for $commonTaskType")
            }

            getFiltersData(commonTaskType).let {
                when (statusType) {
                    StatusType.Status -> it.statuses.map { it.toStatus(statusType) }
                    StatusType.Type -> it.types.map { it.toStatus(statusType) }
                    StatusType.Severity -> it.severities.map { it.toStatus(statusType) }
                    StatusType.Priority -> it.priorities.map { it.toStatus(statusType) }
                }
            }
        }

    @Deprecated("remove it and use the one form epics repo")
    override suspend fun getEpics(page: Int, filters: FiltersData): List<CommonTask> = handle404 {
        epicsApi.getEpics(
            page = page,
            project = currentProjectId,
            query = filters.query,
            assignedIds = filters.assignees.commaString(),
            ownerIds = filters.createdBy.commaString(),
            statuses = filters.statuses.commaString(),
            tags = filters.tags.tagsCommaString()
        )
            .map { it.toCommonTask(CommonTaskType.Epic) }
    }

    override suspend fun getAllUserStories() = withIO {
        val filters = async { getFiltersData(CommonTaskType.UserStory) }
        val swimlanes = async { getSwimlanes() }

        userStoriesApi.getUserStories(project = currentProjectId)
            .map {
                it.toCommonTaskExtended(
                    commonTaskType = CommonTaskType.UserStory,
                    filters = filters.await(),
                    swimlanes = swimlanes.await(),
                    loadSprint = false
                )
            }
    }

    override suspend fun getBacklogUserStories(page: Int, filters: FiltersData) = handle404 {
        userStoriesApi.getUserStories(
            project = currentProjectId,
            sprint = "null",
            page = page,
            query = filters.query,
            assignedIds = filters.assignees.commaString(),
            ownerIds = filters.createdBy.commaString(),
            roles = filters.roles.commaString(),
            statuses = filters.statuses.commaString(),
            epics = filters.epics.commaString(),
            tags = filters.tags.tagsCommaString()
        )
            .map { it.toCommonTask(CommonTaskType.UserStory) }
    }

    override suspend fun getEpicUserStories(epicId: Long) =
        userStoriesApi.getUserStories(epic = epicId)
            .map { it.toCommonTask(CommonTaskType.UserStory) }

    override suspend fun getUserStoryTasks(storyId: Long) = handle404 {
        taigaApi.getTasks(userStory = storyId, project = currentProjectId)
            .map { it.toCommonTask(CommonTaskType.Task) }
    }

    override suspend fun getIssues(page: Int, filters: FiltersData) = withIO {
        handle404 {
            issuesApi.getIssues(
                page = page,
                project = currentProjectId,
                query = filters.query,
                assignedIds = filters.assignees.commaString(),
                ownerIds = filters.createdBy.commaString(),
                priorities = filters.priorities.commaString(),
                severities = filters.severities.commaString(),
                types = filters.types.commaString(),
                statuses = filters.statuses.commaString(),
                roles = filters.roles.commaString(),
                tags = filters.tags.tagsCommaString()
            )
                .map { it.toCommonTask(CommonTaskType.Issue) }
        }
    }

    override suspend fun getCommonTask(commonTaskId: Long, type: CommonTaskType) = withIO {
        val filters = async { getFiltersData(type) }
        val swimlanes = async { getSwimlanes() }

        taigaApi.getCommonTask(CommonTaskPathPlural(type), commonTaskId).toCommonTaskExtended(
            commonTaskType = type,
            filters = filters.await(),
            swimlanes = swimlanes.await()
        )
    }

    override suspend fun getComments(commonTaskId: Long, type: CommonTaskType) = withIO {
        taigaApi.getCommonTaskComments(CommonTaskPathSingular(type), commonTaskId)
            .sortedBy { it.postDateTime }
            .filter { it.deleteDate == null }
            .map { it.also { it.canDelete = it.author.actualId == currentUserId } }
    }

    override suspend fun getAttachments(commonTaskId: Long, type: CommonTaskType) = withIO {
        taigaApi.getCommonTaskAttachments(
            CommonTaskPathPlural(type),
            commonTaskId,
            currentProjectId
        )
    }

    override suspend fun getCustomFields(commonTaskId: Long, type: CommonTaskType) = withIO {
        val attributes =
            async { taigaApi.getCustomAttributes(CommonTaskPathSingular(type), currentProjectId) }
        val values = taigaApi.getCustomAttributesValues(CommonTaskPathPlural(type), commonTaskId)

        CustomFields(
            version = values.version,
            fields = attributes.await().sortedBy { it.order }
                .map {
                    CustomField(
                        id = it.id,
                        type = it.type,
                        name = it.name,
                        description = it.description?.takeIf { it.isNotEmpty() },
                        value = values.attributesValues[it.id]?.let { value ->
                            CustomFieldValue(
                                when (it.type) {
                                    CustomFieldType.Date -> (value as? String)?.takeIf {
                                        it.isNotEmpty()
                                    }?.toLocalDate()

                                    CustomFieldType.Checkbox -> value as? Boolean
                                    else -> value
                                } ?: return@let null
                            )
                        },
                        options = it.extra.orEmpty()
                    )
                }
        )
    }

    override suspend fun getAllTags(commonTaskType: CommonTaskType) = withIO {
        getFiltersData(commonTaskType).tags.map { Tag(it.name, it.color) }
    }

    override suspend fun getSwimlanes() = withIO {
        taigaApi.getSwimlanes(currentProjectId)
    }

    private fun transformTaskTypeForCopyLink(commonTaskType: CommonTaskType) =
        when (commonTaskType) {
            CommonTaskType.UserStory -> PATH_TO_USERSTORY
            CommonTaskType.Task -> PATH_TO_TASK
            CommonTaskType.Epic -> PATH_TO_EPIC
            CommonTaskType.Issue -> PATH_TO_ISSUE
        }

    private val nullOwnerError = IllegalArgumentException(
        "CommonTaskResponse requires not null 'owner' field"
    )

    private suspend fun CommonTaskResponse.toCommonTaskExtended(
        commonTaskType: CommonTaskType,
        filters: FiltersData,
        swimlanes: List<Swimlane>,
        loadSprint: Boolean = true
    ): CommonTaskExtended = CommonTaskExtended(
        id = id,
        status = Status(
            id = status,
            name = statusExtraInfo.name,
            color = statusExtraInfo.color,
            type = StatusType.Status
        ),
        taskType = commonTaskType,
        createdDateTime = createdDate,
        sprint = if (loadSprint) {
            milestone?.let {
                sprintApi.getSprint(it).toSprint()
            }
        } else {
            null
        },
        assignedIds = assignedUsers ?: listOfNotNull(assignedTo),
        watcherIds = watchers.orEmpty(),
        creatorId = owner ?: throw nullOwnerError,
        ref = ref,
        title = subject,
        isClosed = isClosed,
        description = description ?: "",
        epicsShortInfo = epics.orEmpty(),
        projectSlug = projectExtraInfo.slug,
        tags = tags.orEmpty().map { Tag(name = it[0]!!, color = it[1].fixNullColor()) },
        swimlane = swimlanes.find { it.id == swimlane },
        dueDate = dueDate,
        dueDateStatus = dueDateStatus,
        userStoryShortInfo = userStoryExtraInfo,
        version = version,
        color = color,
        type = type?.let { id -> filters.types.find { it.id == id } }
            ?.toStatus(StatusType.Type),
        severity = severity?.let { id -> filters.severities.find { it.id == id } }
            ?.toStatus(StatusType.Severity),
        priority = priority?.let { id -> filters.priorities.find { it.id == id } }
            ?.toStatus(StatusType.Priority),
        url = "${serverStorage.server}/project/${projectExtraInfo.slug}/${
            transformTaskTypeForCopyLink(
                commonTaskType
            )
        }/$ref",
        blockedNote = blockedNote.takeIf { isBlocked }
    )

    /**
     * Edit related
     */

    // edit task itself

    private fun Tag.toList() = listOf(name, color)

    private fun CommonTaskExtended.toEditRequest() = EditCommonTaskRequest(
        subject = title,
        description = description,
        status = status.id,
        type = type?.id,
        severity = severity?.id,
        priority = priority?.id,
        milestone = sprint?.id,
        assignedTo = assignedIds.firstOrNull(),
        assignedUsers = assignedIds,
        watchers = watcherIds,
        swimlane = swimlane?.id,
        dueDate = dueDate,
        color = color,
        tags = tags.map { it.toList() },
        blockedNote = blockedNote.orEmpty(),
        isBlocked = blockedNote != null,
        version = version
    )

    private suspend fun editCommonTask(
        commonTask: CommonTaskExtended,
        request: EditCommonTaskRequest
    ) {
        taigaApi.editCommonTask(CommonTaskPathPlural(commonTask.taskType), commonTask.id, request)
    }

    override suspend fun editStatus(
        commonTask: CommonTaskExtended,
        statusId: Long,
        statusType: StatusType
    ) = withIO {
        if (commonTask.taskType != CommonTaskType.Issue && statusType != StatusType.Status) {
            throw UnsupportedOperationException(
                "Cannot change $statusType for ${commonTask.taskType}"
            )
        }

        val request = commonTask.toEditRequest().let {
            when (statusType) {
                StatusType.Status -> it.copy(status = statusId)
                StatusType.Type -> it.copy(type = statusId)
                StatusType.Severity -> it.copy(severity = statusId)
                StatusType.Priority -> it.copy(priority = statusId)
            }
        }

        editCommonTask(commonTask, request)
    }

    override suspend fun editSprint(commonTask: CommonTaskExtended, sprintId: Long?) = withIO {
        if (commonTask.taskType in listOf(CommonTaskType.Epic, CommonTaskType.Task)) {
            throw UnsupportedOperationException("Cannot change sprint for ${commonTask.taskType}")
        }

        editCommonTask(commonTask, commonTask.toEditRequest().copy(milestone = sprintId))
    }

    override suspend fun editAssignees(commonTask: CommonTaskExtended, assignees: List<Long>) =
        withIO {
            val request = commonTask.toEditRequest().let {
                if (commonTask.taskType == CommonTaskType.UserStory) {
                    it.copy(assignedTo = assignees.firstOrNull(), assignedUsers = assignees)
                } else {
                    it.copy(assignedTo = assignees.lastOrNull())
                }
            }

            editCommonTask(commonTask, request)
        }

    override suspend fun editWatchers(commonTask: CommonTaskExtended, watchers: List<Long>) =
        editCommonTask(commonTask, commonTask.toEditRequest().copy(watchers = watchers))

    override suspend fun editDueDate(commonTask: CommonTaskExtended, date: LocalDate?) =
        editCommonTask(commonTask, commonTask.toEditRequest().copy(dueDate = date))

    override suspend fun editCommonTaskBasicInfo(
        commonTask: CommonTaskExtended,
        title: String,
        description: String
    ) = withIO {
        editCommonTask(
            commonTask,
            commonTask.toEditRequest().copy(subject = title, description = description)
        )
    }

    override suspend fun editTags(commonTask: CommonTaskExtended, tags: List<Tag>) =
        editCommonTask(commonTask, commonTask.toEditRequest().copy(tags = tags.map { it.toList() }))

    override suspend fun editUserStorySwimlane(commonTask: CommonTaskExtended, swimlaneId: Long?) =
        withIO {
            if (commonTask.taskType != CommonTaskType.UserStory) {
                throw UnsupportedOperationException(
                    "Cannot change swimlane for ${commonTask.taskType}"
                )
            }

            editCommonTask(commonTask, commonTask.toEditRequest().copy(swimlane = swimlaneId))
        }

    override suspend fun editEpicColor(commonTask: CommonTaskExtended, color: String) = withIO {
        if (commonTask.taskType != CommonTaskType.Epic) {
            throw UnsupportedOperationException("Cannot change color for ${commonTask.taskType}")
        }

        editCommonTask(commonTask, commonTask.toEditRequest().copy(color = color))
    }

    override suspend fun editBlocked(commonTask: CommonTaskExtended, blockedNote: String?) =
        editCommonTask(
            commonTask,
            commonTask.toEditRequest()
                .copy(isBlocked = blockedNote != null, blockedNote = blockedNote.orEmpty())
        )

    // edit other related parts

    override suspend fun linkToEpic(epicId: Long, userStoryId: Long) = taigaApi.linkToEpic(
        epicId = epicId,
        linkToEpicRequest = LinkToEpicRequest(epicId.toString(), userStoryId)
    )

    override suspend fun unlinkFromEpic(epicId: Long, userStoryId: Long) {
        taigaApi.unlinkFromEpic(epicId, userStoryId)
    }

    override suspend fun createComment(
        commonTaskId: Long,
        commonTaskType: CommonTaskType,
        comment: String,
        version: Int
    ) = taigaApi.createCommonTaskComment(
        taskPath = CommonTaskPathPlural(commonTaskType),
        id = commonTaskId,
        createCommentRequest = CreateCommentRequest(comment, version)
    )

    override suspend fun deleteComment(
        commonTaskId: Long,
        commonTaskType: CommonTaskType,
        commentId: String
    ) = taigaApi.deleteCommonTaskComment(
        taskPath = CommonTaskPathSingular(commonTaskType),
        id = commonTaskId,
        commentId = commentId
    )

    override suspend fun createCommonTask(
        commonTaskType: CommonTaskType,
        title: String,
        description: String,
        parentId: Long?,
        sprintId: Long?,
        statusId: Long?,
        swimlaneId: Long?
    ) = withIO {
        when (commonTaskType) {
            CommonTaskType.Task -> taigaApi.createTask(
                createTaskRequest = CreateTaskRequest(
                    currentProjectId,
                    title,
                    description,
                    sprintId,
                    parentId
                )
            )

            CommonTaskType.Issue -> taigaApi.createIssue(
                createIssueRequest = CreateIssueRequest(
                    currentProjectId,
                    title,
                    description,
                    sprintId
                )
            )

            CommonTaskType.UserStory -> taigaApi.createUserstory(
                createUserStoryRequest = CreateUserStoryRequest(
                    currentProjectId,
                    title,
                    description,
                    statusId,
                    swimlaneId
                )
            )

            else -> taigaApi.createCommonTask(
                taskPath = CommonTaskPathPlural(commonTaskType),
                createRequest = CreateCommonTaskRequest(
                    currentProjectId,
                    title,
                    description,
                    statusId
                )
            )
        }.toCommonTask(commonTaskType)
    }

    override suspend fun deleteCommonTask(commonTaskType: CommonTaskType, commonTaskId: Long) {
        taigaApi.deleteCommonTask(
            taskPath = CommonTaskPathPlural(commonTaskType),
            id = commonTaskId
        )
    }

    override suspend fun promoteCommonTaskToUserStory(
        commonTaskId: Long,
        commonTaskType: CommonTaskType
    ) = withIO {
        if (commonTaskType in listOf(CommonTaskType.Epic, CommonTaskType.UserStory)) {
            throw UnsupportedOperationException("Cannot promote to user story $commonTaskType")
        }

        taigaApi.promoteCommonTaskToUserStory(
            taskPath = CommonTaskPathPlural(commonTaskType),
            taskId = commonTaskId,
            promoteToUserStoryRequest = PromoteToUserStoryRequest(currentProjectId)
        ).first()
            .let {
                taigaApi.getUserStoryByRef(currentProjectId, it)
                    .toCommonTask(CommonTaskType.UserStory)
            }
    }

    override suspend fun addAttachment(
        commonTaskId: Long,
        commonTaskType: CommonTaskType,
        fileName: String,
        inputStream: InputStream
    ) = withIO {
        val file = MultipartBody.Part.createFormData(
            name = "attached_file",
            filename = fileName,
            body = inputStream.readBytes().toRequestBody("*/*".toMediaType())
        )
        val project = MultipartBody.Part.createFormData("project", currentProjectId.toString())
        val objectId = MultipartBody.Part.createFormData("object_id", commonTaskId.toString())

        inputStream.use {
            taigaApi.uploadCommonTaskAttachment(
                taskPath = CommonTaskPathPlural(commonTaskType),
                file = file,
                project = project,
                objectId = objectId
            )
        }
    }

    override suspend fun deleteAttachment(commonTaskType: CommonTaskType, attachmentId: Long) {
        taigaApi.deleteCommonTaskAttachment(
            taskPath = CommonTaskPathPlural(commonTaskType),
            attachmentId = attachmentId
        )
    }

    override suspend fun editCustomFields(
        commonTaskType: CommonTaskType,
        commonTaskId: Long,
        fields: Map<Long, CustomFieldValue?>,
        version: Int
    ) = withIO {
        taigaApi.editCustomAttributesValues(
            taskPath = CommonTaskPathPlural(commonTaskType),
            taskId = commonTaskId,
            editRequest = EditCustomAttributesValuesRequest(
                fields.mapValues { it.value?.value },
                version
            )
        )
    }

    companion object {
        const val PATH_TO_USERSTORY = "us"
        const val PATH_TO_TASK = "task"
        const val PATH_TO_EPIC = "epic"
        const val PATH_TO_ISSUE = "issue"
    }
}
