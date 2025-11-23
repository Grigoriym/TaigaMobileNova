package com.grappim.taigamobile.feature.sprint.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.api.handle404
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.Sprint
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import com.grappim.taigamobile.feature.sprint.domain.SprintData
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.feature.tasks.domain.TasksRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class SprintsRepositoryImpl @Inject constructor(
    private val userStoriesRepository: UserStoriesRepository,
    private val sprintApi: SprintApi,
    private val issuesRepository: IssuesRepository,
    private val taigaStorage: TaigaStorage,
    private val filtersRepository: FiltersRepository,
    private val tasksRepository: TasksRepository
) : SprintsRepository {

    override suspend fun getSprintData(sprintId: Long): Result<SprintData> = resultOf {
        coroutineScope {
            val sprint = getSprint(sprintId = sprintId)
            val statuses = filtersRepository.getStatuses(CommonTaskType.Task)
            val storiesWithTasks = getSprintUserStories(sprintId = sprintId)
                .map {
                    it to async { tasksRepository.getUserStoryTasks(it.id) }
                }
                .associate { (story, tasks) -> story to tasks.await() }
            val issues = getSprintIssues(sprintId = sprintId)
            val storylessTasks = getSprintTasks(sprintId = sprintId)
            SprintData(
                sprint = sprint,
                statusOlds = statuses,
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
        SprintPagingSource(sprintApi, isClosed, taigaStorage)
    }.flow

    override suspend fun getSprintUserStories(sprintId: Long): List<CommonTask> = userStoriesRepository.getUserStories(
        project = taigaStorage.currentProjectIdFlow.first(),
        sprint = sprintId
    )

    override suspend fun getSprints(page: Int, isClosed: Boolean) = handle404 {
        sprintApi.getSprints(taigaStorage.currentProjectIdFlow.first(), page, isClosed)
            .map { it.toSprint() }
    }

    override suspend fun getSprint(sprintId: Long) = sprintApi.getSprint(sprintId).toSprint()

    override suspend fun getSprintTasks(sprintId: Long) = tasksRepository.getTasks(
        userStory = "null",
        project = taigaStorage.currentProjectIdFlow.first(),
        sprint = sprintId
    )

    override suspend fun getSprintIssues(sprintId: Long) = issuesRepository.getIssues(
        project = taigaStorage.currentProjectIdFlow.first(),
        sprint = sprintId
    )

    override suspend fun createSprint(name: String, start: LocalDate, end: LocalDate): Result<Unit> = resultOf {
        sprintApi.createSprint(
            CreateSprintRequest(
                name,
                start,
                end,
                taigaStorage.currentProjectIdFlow.first()
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
