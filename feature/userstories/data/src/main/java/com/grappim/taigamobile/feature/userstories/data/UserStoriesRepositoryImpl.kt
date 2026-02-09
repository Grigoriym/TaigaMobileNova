package com.grappim.taigamobile.feature.userstories.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.db.dao.WorkItemDao
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.userstories.domain.UpdatedKanbanStory
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.userstories.dto.BulkUpdateKanbanOrderRequest
import com.grappim.taigamobile.feature.userstories.dto.CreateUserStoryRequest
import com.grappim.taigamobile.feature.userstories.mapper.UserStoryMapper
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.data.WorkItemEntityMapper
import com.grappim.taigamobile.feature.workitem.data.WorkItemRemoteMediator
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val userStoryPlural = WorkItemPathPlural(CommonTaskType.UserStory)

class UserStoriesRepositoryImpl @Inject constructor(
    private val userStoriesApi: UserStoriesApi,
    private val taigaSessionStorage: TaigaSessionStorage,
    private val userStoryMapper: UserStoryMapper,
    private val workItemApi: WorkItemApi,
    private val workItemRepository: WorkItemRepository,
    private val workItemMapper: WorkItemMapper,
    private val workItemEntityMapper: WorkItemEntityMapper,
    private val workItemDao: WorkItemDao
) : UserStoriesRepository {
    private var userStoriesPagingSource: UserStoriesPagingSource? = null

    @OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
    override fun getUserStoriesPaging(filters: FiltersData, query: String): Flow<PagingData<WorkItem>> {
        val hasFilters = filters.filtersNumber > 0 || query.isNotBlank()
        if (hasFilters) {
            return Pager(
                PagingConfig(
                    pageSize = 10,
                    enablePlaceholders = false
                )
            ) {
                UserStoriesPagingSource(
                    filters = filters,
                    taigaSessionStorage = taigaSessionStorage,
                    query = query,
                    workItemMapper = workItemMapper,
                    workItemApi = workItemApi
                ).also {
                    userStoriesPagingSource = it
                }
            }.flow
        }
        return taigaSessionStorage.currentProjectIdFlow.flatMapLatest { projectId ->
            Pager(
                config = PagingConfig(
                    pageSize = 10,
                    enablePlaceholders = false
                ),
                remoteMediator = WorkItemRemoteMediator(
                    taskType = CommonTaskType.UserStory,
                    workItemApi = workItemApi,
                    workItemDao = workItemDao,
                    workItemMapper = workItemMapper,
                    workItemEntityMapper = workItemEntityMapper,
                    taigaSessionStorage = taigaSessionStorage
                ),
                pagingSourceFactory = { workItemDao.pagingSource(projectId, CommonTaskType.UserStory) }
            ).flow.map { pagingData ->
                pagingData.map { entity -> workItemEntityMapper.toDomain(entity) }
            }
        }
    }

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

    override suspend fun getUserStories(
        assignedId: Long?,
        isClosed: Boolean?,
        isDashboard: Boolean?,
        watcherId: Long?,
        epicId: Long?,
        project: Long?,
        sprint: Any?
    ): ImmutableList<UserStory> {
        val stories = userStoriesApi.getUserStories(
            assignedId = assignedId,
            isClosed = isClosed,
            isDashboard = isDashboard,
            watcherId = watcherId,
            epic = epicId,
            project = project,
            sprint = sprint
        )
        return userStoryMapper.toListDomain(stories)
    }

    override suspend fun getEpicUserStoriesSimplified(epicId: Long): ImmutableList<WorkItem> =
        userStoriesApi.getUserStories(
            epic = epicId
        ).map {
            workItemMapper.toDomain(it, CommonTaskType.UserStory)
        }.toImmutableList()

    override suspend fun createUserStory(
        subject: String,
        description: String,
        status: Long?,
        swimlane: Long?
    ): WorkItem {
        val response = userStoriesApi.createUserStory(
            createUserStoryRequest = CreateUserStoryRequest(
                project = taigaSessionStorage.getCurrentProjectId(),
                subject = subject,
                description = description,
                status = status,
                swimlane = swimlane
            )
        )
        return workItemMapper.toDomain(response, taskType = CommonTaskType.UserStory)
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

    override suspend fun bulkUpdateKanbanOrder(
        statusId: Long,
        storyIds: List<Long>,
        swimlaneId: Long?,
        afterStoryId: Long?,
        beforeStoryId: Long?
    ): ImmutableList<UpdatedKanbanStory> {
        val response = userStoriesApi.bulkUpdateKanbanOrder(
            request = BulkUpdateKanbanOrderRequest(
                projectId = taigaSessionStorage.getCurrentProjectId(),
                statusId = statusId,
                bulkUserstories = storyIds,
                swimlaneId = swimlaneId,
                afterUserstoryId = afterStoryId,
                beforeUserstoryId = beforeStoryId
            )
        )
        return response.map { item ->
            UpdatedKanbanStory(
                id = item.id,
                status = item.status,
                kanbanOrder = item.kanbanOrder,
                swimlane = item.swimlane
            )
        }.toImmutableList()
    }
}
