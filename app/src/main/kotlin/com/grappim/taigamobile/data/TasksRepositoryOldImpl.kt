package com.grappim.taigamobile.data

import com.grappim.taigamobile.core.api.CustomFieldsMapper
import com.grappim.taigamobile.core.api.toCommonTask
import com.grappim.taigamobile.core.api.withIO
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskExtended
import com.grappim.taigamobile.core.domain.CommonTaskPathPlural
import com.grappim.taigamobile.core.domain.CommonTaskPathSingular
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.CustomFieldValue
import com.grappim.taigamobile.core.domain.CustomFields
import com.grappim.taigamobile.core.domain.StatusType
import com.grappim.taigamobile.core.domain.Tag
import com.grappim.taigamobile.core.domain.TasksRepositoryOld
import com.grappim.taigamobile.core.domain.toCommonTaskExtended
import com.grappim.taigamobile.core.domain.transformTaskTypeForCopyLink
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.data.api.TaigaApi
import com.grappim.taigamobile.data.model.CreateCommentRequest
import com.grappim.taigamobile.data.model.CreateCommonTaskRequest
import com.grappim.taigamobile.data.model.EditCommonTaskRequest
import com.grappim.taigamobile.data.model.EditCustomAttributesValuesRequest
import com.grappim.taigamobile.data.model.PromoteToUserStoryRequest
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.feature.swimlanes.domain.SwimlanesRepository
import com.grappim.taigamobile.feature.tasks.data.CreateTaskRequest
import com.grappim.taigamobile.feature.tasks.data.TasksApi
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import com.grappim.taigamobile.utils.ui.fixNullColor
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import java.time.LocalDate
import javax.inject.Inject

class TasksRepositoryOldImpl @Inject constructor(
    private val taigaApi: TaigaApi,
    private val userStoriesRepository: UserStoriesRepository,
    private val sprintsRepository: SprintsRepository,
    private val serverStorage: ServerStorage,
    private val tasksApi: TasksApi,
    private val taigaStorage: TaigaStorage,
    private val filtersRepository: FiltersRepository,
    private val swimlanesRepository: SwimlanesRepository,
    private val issuesRepository: IssuesRepository,
    private val customFieldsMapper: CustomFieldsMapper
) : TasksRepositoryOld {

    override suspend fun getCommonTask(commonTaskId: Long, type: CommonTaskType) = withIO {
        val filters = async { filtersRepository.getFiltersDataOld(type) }
        val swimlanes = async { swimlanesRepository.getSwimlanes() }

        val task = taigaApi.getCommonTask(CommonTaskPathPlural(type), commonTaskId)
        val sprint = task.milestone?.let {
            sprintsRepository.getSprint(it)
        }
        task.toCommonTaskExtended(
            commonTaskType = type,
            filters = filters.await(),
            swimlaneDTOS = swimlanes.await(),
            sprint = sprint,
            tags = task.tags.orEmpty()
                .map { Tag(name = it[0]!!, color = it[1].fixNullColor()) },
            url = "${serverStorage.server}/project/${task.projectDTOExtraInfo.slug}/${
                transformTaskTypeForCopyLink(type)
            }/${task.ref}"
        )
    }

    override suspend fun getAttachments(commonTaskId: Long, type: CommonTaskType) = taigaApi.getCommonTaskAttachments(
        CommonTaskPathPlural(type),
        commonTaskId,
        taigaStorage.currentProjectIdFlow.first()
    )

    override suspend fun getCustomFields(commonTaskId: Long, type: CommonTaskType): CustomFields = coroutineScope {
        val attributes =
            async {
                taigaApi.getCustomAttributes(
                    CommonTaskPathSingular(type),
                    taigaStorage.currentProjectIdFlow.first()
                )
            }
        val values = taigaApi.getCustomAttributesValues(
            taskPath = CommonTaskPathPlural(commonTaskType = type),
            taskId = commonTaskId
        )

        customFieldsMapper.toDomain(
            attributes = attributes.await(),
            values = values
        )
    }

    /**
     * Edit related
     */

    // edit task itself

    private fun Tag.toList() = listOf(name, color)

    private fun CommonTaskExtended.toEditRequest() = EditCommonTaskRequest(
        subject = title,
        description = description,
        status = statusOld.id,
        type = type?.id,
        severity = severity?.id,
        priority = priority?.id,
        milestone = sprint?.id,
        assignedTo = assignedIds.firstOrNull(),
        assignedUsers = assignedIds,
        watchers = watcherIds,
        swimlane = swimlaneDTO?.id,
        dueDate = dueDate,
        color = color,
        tags = tags.map { it.toList() },
        blockedNote = blockedNote.orEmpty(),
        isBlocked = blockedNote != null,
        version = version
    )

    private suspend fun editCommonTask(commonTask: CommonTaskExtended, request: EditCommonTaskRequest) {
        taigaApi.editCommonTask(CommonTaskPathPlural(commonTask.taskType), commonTask.id, request)
    }

    override suspend fun editStatus(commonTask: CommonTaskExtended, statusId: Long, statusType: StatusType) = withIO {
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

    override suspend fun editAssignees(commonTask: CommonTaskExtended, assignees: List<Long>) = withIO {
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

    override suspend fun editCommonTaskBasicInfo(commonTask: CommonTaskExtended, title: String, description: String) =
        withIO {
            editCommonTask(
                commonTask,
                commonTask.toEditRequest().copy(subject = title, description = description)
            )
        }

    override suspend fun editTags(commonTask: CommonTaskExtended, tags: List<Tag>) =
        editCommonTask(commonTask, commonTask.toEditRequest().copy(tags = tags.map { it.toList() }))

    override suspend fun editUserStorySwimlane(commonTask: CommonTaskExtended, swimlaneId: Long?) = withIO {
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

    override suspend fun editBlocked(commonTask: CommonTaskExtended, blockedNote: String?) = editCommonTask(
        commonTask,
        commonTask.toEditRequest()
            .copy(isBlocked = blockedNote != null, blockedNote = blockedNote.orEmpty())
    )

    override suspend fun createComment(
        commonTaskId: Long,
        commonTaskType: CommonTaskType,
        comment: String,
        version: Long
    ) = taigaApi.createCommonTaskComment(
        taskPath = CommonTaskPathPlural(commonTaskType),
        id = commonTaskId,
        createCommentRequest = CreateCommentRequest(comment, version)
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
            CommonTaskType.Task -> tasksApi.createTask(
                createTaskRequest = CreateTaskRequest(
                    taigaStorage.currentProjectIdFlow.first(),
                    title,
                    description,
                    sprintId,
                    parentId
                )
            )

            CommonTaskType.Issue -> issuesRepository.createIssue(
                title = title,
                description = description,
                sprintId = sprintId
            )

            CommonTaskType.UserStory -> userStoriesRepository.createUserStory(
                project = taigaStorage.currentProjectIdFlow.first(),
                subject = title,
                description = description,
                status = statusId,
                swimlane = swimlaneId
            )

            else -> taigaApi.createCommonTask(
                taskPath = CommonTaskPathPlural(commonTaskType),
                createRequest = CreateCommonTaskRequest(
                    taigaStorage.currentProjectIdFlow.first(),
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

    override suspend fun promoteCommonTaskToUserStory(commonTaskId: Long, commonTaskType: CommonTaskType): CommonTask =
        withIO {
            if (commonTaskType in listOf(CommonTaskType.Epic, CommonTaskType.UserStory)) {
                throw UnsupportedOperationException("Cannot promote to user story $commonTaskType")
            }

            taigaApi.promoteCommonTaskToUserStory(
                taskPath = CommonTaskPathPlural(commonTaskType),
                taskId = commonTaskId,
                promoteToUserStoryRequest = PromoteToUserStoryRequest(
                    taigaStorage.currentProjectIdFlow.first()
                )
            ).first()
                .let {
                    userStoriesRepository.getUserStoryByRefOld(
                        projectId = taigaStorage.currentProjectIdFlow.first(),
                        ref = it
                    )
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
        val project = MultipartBody.Part.createFormData(
            "project",
            taigaStorage.currentProjectIdFlow.first().toString()
        )
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
        version: Long
    ) {
        taigaApi.editCustomAttributesValues(
            taskPath = CommonTaskPathPlural(commonTaskType),
            taskId = commonTaskId,
            editRequest = EditCustomAttributesValuesRequest(
                fields.mapValues { it.value?.value },
                version
            )
        )
    }
}
