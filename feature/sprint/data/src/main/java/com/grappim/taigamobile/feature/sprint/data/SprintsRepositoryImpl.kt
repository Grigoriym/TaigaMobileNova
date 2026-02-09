package com.grappim.taigamobile.feature.sprint.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.db.dao.SprintDao
import com.grappim.taigamobile.core.storage.db.dao.WorkItemDao
import com.grappim.taigamobile.core.storage.network.NetworkMonitor
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.feature.sprint.domain.SprintData
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.data.WorkItemEntityMapper
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

class SprintsRepositoryImpl @Inject constructor(
    private val sprintApi: SprintApi,
    private val taigaSessionStorage: TaigaSessionStorage,
    private val filtersRepository: FiltersRepository,
    private val workItemMapper: WorkItemMapper,
    private val workItemEntityMapper: WorkItemEntityMapper,
    private val workItemApi: WorkItemApi,
    private val sprintMapper: SprintMapper,
    private val sprintDao: SprintDao,
    private val workItemDao: WorkItemDao,
    private val networkMonitor: NetworkMonitor
) : SprintsRepository {

    override suspend fun getSprintData(sprintId: Long): Result<SprintData> = resultOf {
        coroutineScope {
            val sprint = getSprint(sprintId = sprintId)
            val statuses = filtersRepository.getStatuses(CommonTaskType.Task)
            val storiesWithTasks = getSprintUserStories(sprintId = sprintId)
                .map {
                    it to async {
                        val dtos = workItemApi.getWorkItems(
                            taskPath = WorkItemPathPlural(CommonTaskType.Task),
                            project = taigaSessionStorage.getCurrentProjectId(),
                            userStory = it.id
                        )
                        workItemMapper.toDomainList(dtos, CommonTaskType.Task)
                    }
                }
                .associate { (story, tasks) -> story to tasks.await() }
                .toImmutableMap()
            val issues = getSprintIssues(sprintId = sprintId)
            val storylessTasks = getSprintTasks(sprintId = sprintId)
            SprintData(
                sprint = sprint,
                statuses = statuses,
                storiesWithTasks = storiesWithTasks,
                issues = issues,
                storylessTasks = storylessTasks
            )
        }
    }

    @OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
    override fun getSprintsPaging(isClosed: Boolean): Flow<PagingData<Sprint>> =
        taigaSessionStorage.currentProjectIdFlow.flatMapLatest { projectId ->
            Pager(
                config = PagingConfig(
                    pageSize = 10,
                    enablePlaceholders = false
                ),
                remoteMediator = SprintRemoteMediator(
                    isClosed = isClosed,
                    sprintApi = sprintApi,
                    sprintDao = sprintDao,
                    sprintMapper = sprintMapper,
                    taigaSessionStorage = taigaSessionStorage
                ),
                pagingSourceFactory = { sprintDao.pagingSource(projectId, isClosed) }
            ).flow.map { pagingData ->
                pagingData.map { entity -> entity.toDomain() }
            }
        }

    override suspend fun getSprintUserStories(sprintId: Long): ImmutableList<WorkItem> {
        val projectId = taigaSessionStorage.getCurrentProjectId()

        if (networkMonitor.isOnline.value) {
            try {
                val response = workItemApi.getWorkItems(
                    taskPath = WorkItemPathPlural(CommonTaskType.UserStory),
                    project = projectId,
                    sprint = sprintId
                )
                val items = workItemMapper.toDomainList(response, CommonTaskType.UserStory)
                // Cache results
                workItemDao.deleteByProjectIdAndType(projectId, CommonTaskType.UserStory)
                workItemDao.insertAll(workItemEntityMapper.toEntityList(items, sprintId))
                return items
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        return workItemDao.getByProjectIdAndSprint(projectId, sprintId)
            .first()
            .filter { it.taskType == CommonTaskType.UserStory }
            .let { workItemEntityMapper.toDomainList(it) }
            .toImmutableList()
    }

    override suspend fun getSprints(isClosed: Boolean): ImmutableList<Sprint> {
        val projectId = taigaSessionStorage.getCurrentProjectId()

        // Try network first if online
        if (networkMonitor.isOnline.value) {
            try {
                val dtos = sprintApi.getSprints(project = projectId, isClosed = isClosed)
                val sprints = sprintMapper.toDomainList(dtos)
                // Cache the results
                sprintDao.deleteByProjectId(projectId)
                sprintDao.insertAll(sprints.toEntityList(projectId))
                return sprints
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        // Return cached data
        val cached = sprintDao.getByProjectId(projectId).first()
            .filter { it.isClosed == isClosed }
            .toDomainList()
        return cached.toImmutableList()
    }

    override suspend fun getSprint(sprintId: Long): Sprint {
        val sprint = sprintApi.getSprint(sprintId)
        return sprintMapper.toDomain(sprint)
    }

    override suspend fun getSprintTasks(sprintId: Long): ImmutableList<WorkItem> {
        val projectId = taigaSessionStorage.getCurrentProjectId()

        if (networkMonitor.isOnline.value) {
            try {
                val response = workItemApi.getWorkItems(
                    taskPath = WorkItemPathPlural(CommonTaskType.Task),
                    project = projectId,
                    sprint = sprintId,
                    userStory = "null"
                )
                val items = workItemMapper.toDomainList(response, CommonTaskType.Task)
                workItemDao.insertAll(workItemEntityMapper.toEntityList(items, sprintId))
                return items
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        return workItemDao.getByProjectIdAndSprint(projectId, sprintId)
            .first()
            .filter { it.taskType == CommonTaskType.Task }
            .let { workItemEntityMapper.toDomainList(it) }
            .toImmutableList()
    }

    override suspend fun getSprintIssues(sprintId: Long): ImmutableList<WorkItem> {
        val projectId = taigaSessionStorage.getCurrentProjectId()

        if (networkMonitor.isOnline.value) {
            try {
                val response = workItemApi.getWorkItems(
                    taskPath = WorkItemPathPlural(CommonTaskType.Issue),
                    project = projectId,
                    sprint = sprintId
                )
                val items = workItemMapper.toDomainList(response, CommonTaskType.Issue)
                workItemDao.insertAll(workItemEntityMapper.toEntityList(items, sprintId))
                return items
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        return workItemDao.getByProjectIdAndSprint(projectId, sprintId)
            .first()
            .filter { it.taskType == CommonTaskType.Issue }
            .let { workItemEntityMapper.toDomainList(it) }
            .toImmutableList()
    }

    override suspend fun createSprint(name: String, start: LocalDate, end: LocalDate) {
        sprintApi.createSprint(
            CreateSprintRequest(
                name = name,
                estimatedStart = start,
                estimatedFinish = end,
                project = taigaSessionStorage.getCurrentProjectId()
            )
        )
    }

    override suspend fun editSprint(sprintId: Long, name: String, start: LocalDate, end: LocalDate) =
        sprintApi.editSprint(
            id = sprintId,
            request = EditSprintRequest(name, start, end)
        )

    override suspend fun deleteSprint(sprintId: Long) {
        sprintApi.deleteSprint(sprintId)
    }
}
