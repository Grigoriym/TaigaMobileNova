package com.grappim.taigamobile.feature.sprint.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.feature.sprint.domain.SprintData
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class SprintsRepositoryImpl @Inject constructor(
    private val sprintApi: SprintApi,
    private val taigaStorage: TaigaStorage,
    private val filtersRepository: FiltersRepository,
    private val workItemMapper: WorkItemMapper,
    private val workItemApi: WorkItemApi,
    private val sprintMapper: SprintMapper
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
                            project = taigaStorage.currentProjectIdFlow.first(),
                            userStory = it.id
                        )
                        workItemMapper.toDomainList(dtos, CommonTaskType.Task)
                    }
                }
                .associate { (story, tasks) -> story to tasks.await() }
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

    override fun getSprints(isClosed: Boolean): Flow<PagingData<Sprint>> = Pager(
        PagingConfig(
            pageSize = 20,
            enablePlaceholders = false
        )
    ) {
        SprintPagingSource(
            sprintApi = sprintApi,
            isClosed = isClosed,
            taigaStorage = taigaStorage,
            sprintMapper = sprintMapper
        )
    }.flow

    override suspend fun getSprintUserStories(sprintId: Long): ImmutableList<WorkItem> {
        val response = workItemApi.getWorkItems(
            taskPath = WorkItemPathPlural(CommonTaskType.UserStory),
            project = taigaStorage.currentProjectIdFlow.first(),
            sprint = sprintId
        )
        return workItemMapper.toDomainList(response, CommonTaskType.UserStory)
    }

    override suspend fun getSprints(page: Int, isClosed: Boolean): ImmutableList<Sprint> {
        val dtos = sprintApi.getSprints(
            project = taigaStorage.currentProjectIdFlow.first(),
            page = page,
            isClosed = isClosed
        )
        return sprintMapper.toDomainList(dtos)
    }

    override suspend fun getSprint(sprintId: Long): Sprint {
        val sprint = sprintApi.getSprint(sprintId)
        return sprintMapper.toDomain(sprint)
    }

    override suspend fun getSprintTasks(sprintId: Long): ImmutableList<WorkItem> {
        val response = workItemApi.getWorkItems(
            taskPath = WorkItemPathPlural(CommonTaskType.Task),
            project = taigaStorage.currentProjectIdFlow.first(),
            sprint = sprintId,
            userStory = "null"
        )
        return workItemMapper.toDomainList(response, CommonTaskType.Task)
    }

    override suspend fun getSprintIssues(sprintId: Long): ImmutableList<WorkItem> {
        val response = workItemApi.getWorkItems(
            taskPath = WorkItemPathPlural(CommonTaskType.Issue),
            project = taigaStorage.currentProjectIdFlow.first(),
            sprint = sprintId
        )
        return workItemMapper.toDomainList(response, CommonTaskType.Issue)
    }

    override suspend fun createSprint(name: String, start: LocalDate, end: LocalDate) {
        sprintApi.createSprint(
            CreateSprintRequest(
                name = name,
                estimatedStart = start,
                estimatedFinish = end,
                project = taigaStorage.currentProjectIdFlow.first()
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
