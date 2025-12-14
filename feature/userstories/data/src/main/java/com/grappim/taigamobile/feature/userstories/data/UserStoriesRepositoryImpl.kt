package com.grappim.taigamobile.feature.userstories.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.api.CommonTaskMapper
import com.grappim.taigamobile.core.api.UserMapper
import com.grappim.taigamobile.core.api.handle404
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.domain.commaString
import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.core.domain.tagsCommaString
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.filters.data.StatusMapper
import com.grappim.taigamobile.feature.filters.data.TagsMapper
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.data.WorkItemResponseDTO
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private val userStoryPlural = WorkItemPathPlural(CommonTaskType.UserStory)

class UserStoriesRepositoryImpl @Inject constructor(
    private val userStoriesApi: UserStoriesApi,
    private val taigaStorage: TaigaStorage,
    private val commonTaskMapper: CommonTaskMapper,
    private val userStoryMapper: UserStoryMapper,
    private val workItemApi: WorkItemApi,
    private val workItemRepository: WorkItemRepository,
    private val statusMapper: StatusMapper,
    private val userMapper: UserMapper,
    private val tagsMapper: TagsMapper
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

    override suspend fun getUserStories(): ImmutableList<UserStory> {
        val stories = userStoriesApi.getUserStories(
            project = taigaStorage.currentProjectIdFlow.first()
        )
        return userStoryMapper.toListDomain(stories)
    }

    override suspend fun getBacklogUserStories(page: Int, filters: FiltersDataDTO) = handle404 {
        userStoriesApi.getUserStoriesOld(
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

    override suspend fun getUserStoriesOld(
        assignedId: Long?,
        isClosed: Boolean?,
        isDashboard: Boolean?,
        watcherId: Long?,
        epicId: Long?,
        project: Long?,
        sprint: Any?
    ): List<CommonTask> = userStoriesApi.getUserStoriesOld(
        assignedId = assignedId,
        isClosed = isClosed,
        isDashboard = isDashboard,
        watcherId = watcherId,
        epic = epicId,
        project = project,
        sprint = sprint
    ).map { commonTaskMapper.toDomain(it, CommonTaskType.UserStory) }

    override suspend fun getEpicUserStoriesSimplified(epicId: Long): ImmutableList<WorkItem> =
        userStoriesApi.getUserStories(
            epic = epicId
        ).map {
            toDomain(it, CommonTaskType.UserStory)
        }.toImmutableList()

    private suspend fun toDomain(dto: WorkItemResponseDTO, taskType: CommonTaskType): WorkItem = WorkItem(
        id = dto.id,
        taskType = taskType,
        createdDate = dto.createdDate,
        status = statusMapper.getStatus(dto),
        ref = dto.ref,
        title = dto.subject,
        isBlocked = dto.isBlocked,
        tags = tagsMapper.toTags(dto.tags),
        isClosed = dto.isClosed,
        colors = dto.color?.let {
            persistentListOf(it)
        } ?: dto.epics.orEmpty().map {
            it.color
        }.toPersistentList(),
        assignee = dto.assignedToExtraInfo?.let { assigned ->
            userMapper.toUser(assigned)
        }
    )

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

    override suspend fun patchData(version: Long, userStoryId: Long, payload: ImmutableMap<String, Any?>): PatchedData =
        workItemRepository.patchData(
            commonTaskType = CommonTaskType.UserStory,
            workItemId = userStoryId,
            payload = payload,
            version = version
        )

    override suspend fun deleteUserStory(id: Long) {
        workItemApi.deleteWorkItem(
            taskPath = userStoryPlural,
            workItemId = id
        )
    }
}
