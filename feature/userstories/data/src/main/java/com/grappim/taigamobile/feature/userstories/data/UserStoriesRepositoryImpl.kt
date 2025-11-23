package com.grappim.taigamobile.feature.userstories.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.api.AttachmentMapper
import com.grappim.taigamobile.core.api.CommonTaskMapper
import com.grappim.taigamobile.core.api.CustomFieldsMapper
import com.grappim.taigamobile.core.api.handle404
import com.grappim.taigamobile.core.api.withIO
import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.CustomFields
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.domain.Tag
import com.grappim.taigamobile.core.domain.commaString
import com.grappim.taigamobile.core.domain.patch.PatchedCustomAttributes
import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.core.domain.tagsCommaString
import com.grappim.taigamobile.core.domain.toCommonTaskExtended
import com.grappim.taigamobile.core.domain.transformTaskTypeForCopyLink
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.swimlanes.domain.SwimlanesRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathSingular
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.utils.ui.fixNullColor
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private val userStoryPlural = WorkItemPathPlural(CommonTaskType.UserStory)
private val userStorySingular = WorkItemPathSingular(CommonTaskType.UserStory)

class UserStoriesRepositoryImpl @Inject constructor(
    private val userStoriesApi: UserStoriesApi,
    private val taigaStorage: TaigaStorage,
    private val filtersRepository: FiltersRepository,
    private val swimlanesRepository: SwimlanesRepository,
    private val serverStorage: ServerStorage,
    private val commonTaskMapper: CommonTaskMapper,
    private val userStoryMapper: UserStoryMapper,
    private val attachmentMapper: AttachmentMapper,
    private val workItemApi: WorkItemApi,
    private val customFieldsMapper: CustomFieldsMapper,
    private val workItemRepository: WorkItemRepository
) : UserStoriesRepository {
    private var userStoriesPagingSource: UserStoriesPagingSource? = null

    override fun getUserStoriesPaging(filters: FiltersDataDTO): Flow<PagingData<CommonTask>> = Pager(
        PagingConfig(
            pageSize = 20,
            enablePlaceholders = false
        )
    ) {
        UserStoriesPagingSource(userStoriesApi, filters, taigaStorage).also {
            userStoriesPagingSource = it
        }
    }.flow

    override fun refreshUserStories() {
        userStoriesPagingSource?.invalidate()
    }

    override suspend fun getUserStory(id: Long): UserStory {
        val response = workItemApi.getWorkItemById(
            taskPath = userStoryPlural,
            id = id
        )
        return userStoryMapper.toDomain(resp = response)
    }

    override suspend fun getAllUserStories() = withIO {
        val filters = async { filtersRepository.getFiltersDataOld(CommonTaskType.UserStory) }
        val swimlanes = async { swimlanesRepository.getSwimlanes() }

        userStoriesApi.getUserStories(project = taigaStorage.currentProjectIdFlow.first())
            .map { response ->
                response.toCommonTaskExtended(
                    commonTaskType = CommonTaskType.UserStory,
                    filters = filters.await(),
                    swimlaneDTOS = swimlanes.await(),
                    tags = response.tags.orEmpty()
                        .map { Tag(name = it[0]!!, color = it[1].fixNullColor()) },
                    url = "${serverStorage.server}/project/${response.projectDTOExtraInfo.slug}/${
                        transformTaskTypeForCopyLink(
                            CommonTaskType.UserStory
                        )
                    }/${response.ref}"
                )
            }
    }

    override suspend fun getBacklogUserStories(page: Int, filters: FiltersDataDTO) = handle404 {
        userStoriesApi.getUserStories(
            project = taigaStorage.currentProjectIdFlow.first(),
            sprint = "null",
            page = page,
            query = filters.query,
            assignedIds = filters.assignees.commaString(),
            ownerIds = filters.createdBy.commaString(),
            roles = filters.roles.commaString(),
            statuses = filters.statuses.commaString(),
            epics = filters.epics.commaString(),
            tags = filters.tags.tagsCommaString()
        ).map { commonTaskMapper.toDomain(it, CommonTaskType.UserStory) }
    }

    override suspend fun getUserStories(
        assignedId: Long?,
        isClosed: Boolean?,
        isDashboard: Boolean?,
        watcherId: Long?,
        epicId: Long?,
        project: Long?,
        sprint: Any?
    ): List<CommonTask> = userStoriesApi.getUserStories(
        assignedId = assignedId,
        isClosed = isClosed,
        isDashboard = isDashboard,
        watcherId = watcherId,
        epic = epicId,
        project = project,
        sprint = sprint
    ).map { commonTaskMapper.toDomain(it, CommonTaskType.UserStory) }

    override suspend fun createUserStory(
        project: Long,
        subject: String,
        description: String,
        status: Long?,
        swimlane: Long?
    ): CommonTaskResponse = userStoriesApi.createUserStory(
        createUserStoryRequest = CreateUserStoryRequest(
            project = project,
            subject = subject,
            description = description,
            status = status,
            swimlane = swimlane
        )
    )

    override suspend fun getUserStoryByRefOld(projectId: Long, ref: Int): CommonTask {
        val response = userStoriesApi.getUserStoryByRef(
            projectId = projectId,
            ref = ref
        )
        return commonTaskMapper.toDomain(response, CommonTaskType.UserStory)
    }

    override suspend fun getUserStoryAttachments(taskId: Long): List<Attachment> {
        val projectId = taigaStorage.currentProjectIdFlow.first()

        val result = workItemApi.getAttachments(
            taskPath = userStoryPlural,
            objectId = taskId,
            projectId = projectId
        )
        return attachmentMapper.toDomain(result)
    }

    override suspend fun getCustomFields(id: Long): CustomFields = coroutineScope {
        val attributes = async {
            workItemApi.getCustomAttributes(
                taskPath = userStorySingular,
                projectId = taigaStorage.currentProjectIdFlow.first()
            )
        }
        val values = async {
            workItemApi.getCustomAttributesValues(
                taskPath = userStoryPlural,
                id = id
            )
        }

        customFieldsMapper.toDomain(
            attributes = attributes.await(),
            values = values.await()
        )
    }

    override suspend fun patchData(version: Long, userStoryId: Long, payload: ImmutableMap<String, Any?>): PatchedData =
        workItemRepository.patchData(
            commonTaskType = CommonTaskType.UserStory,
            workItemId = userStoryId,
            payload = payload,
            version = version
        )

    override suspend fun patchCustomAttributes(
        version: Long,
        userStoryId: Long,
        payload: ImmutableMap<String, Any?>
    ): PatchedCustomAttributes = workItemRepository.patchCustomAttributes(
        commonTaskType = CommonTaskType.UserStory,
        workItemId = userStoryId,
        payload = payload,
        version = version
    )

    override suspend fun unwatchUserStory(userStoryId: Long) {
        workItemApi.unwatchWorkItem(
            taskPath = userStoryPlural,
            workItemId = userStoryId
        )
    }

    override suspend fun watchUserStory(userStoryId: Long) {
        workItemRepository.watchWorkItem(
            workItemId = userStoryId,
            commonTaskType = CommonTaskType.UserStory
        )
    }

    override suspend fun deleteIssue(id: Long) {
        workItemApi.deleteWorkItem(
            taskPath = userStoryPlural,
            workItemId = id
        )
    }

    override suspend fun deleteAttachment(attachment: Attachment) {
        workItemRepository.deleteAttachment(
            commonTaskType = CommonTaskType.UserStory,
            attachment = attachment
        )
    }

    override suspend fun addAttachment(id: Long, fileName: String, fileByteArray: ByteArray): Attachment =
        workItemRepository.addAttachment(
            workItemId = id,
            fileName = fileName,
            fileByteArray = fileByteArray,
            projectId = taigaStorage.currentProjectIdFlow.first(),
            commonTaskType = CommonTaskType.UserStory
        )
}
