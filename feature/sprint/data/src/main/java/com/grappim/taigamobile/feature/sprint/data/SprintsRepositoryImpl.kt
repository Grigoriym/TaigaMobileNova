package com.grappim.taigamobile.feature.sprint.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.api.handle404
import com.grappim.taigamobile.core.api.toCommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.Sprint
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.issues.data.IssuesApi
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.feature.tasks.data.TasksApi
import com.grappim.taigamobile.feature.userstories.data.UserStoriesApi
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class SprintsRepositoryImpl @Inject constructor(
    private val session: Session,
    private val userStoriesApi: UserStoriesApi,
    private val sprintApi: SprintApi,
    private val issuesApi: IssuesApi,
    private val tasksApi: TasksApi
) : SprintsRepository {
    override fun getSprints(isClosed: Boolean): Flow<PagingData<Sprint>> = Pager(
        PagingConfig(
            pageSize = 20,
            enablePlaceholders = false
        )
    ) {
        SprintPagingSource(sprintApi, isClosed, session)
    }.flow

    private val currentProjectId get() = session.currentProject

    override suspend fun getSprintUserStories(sprintId: Long) =
        userStoriesApi.getUserStories(project = currentProjectId, sprint = sprintId)
            .map { it.toCommonTask(CommonTaskType.UserStory) }

    override suspend fun getSprints(page: Int, isClosed: Boolean) = handle404 {
        sprintApi.getSprints(currentProjectId, page, isClosed).map { it.toSprint() }
    }

    override suspend fun getSprint(sprintId: Long) = sprintApi.getSprint(sprintId).toSprint()

    override suspend fun getSprintTasks(sprintId: Long) =
        tasksApi.getTasks(userStory = "null", project = currentProjectId, sprint = sprintId)
            .map { it.toCommonTask(CommonTaskType.Task) }

    override suspend fun getSprintIssues(sprintId: Long) =
        issuesApi.getIssues(project = currentProjectId, sprint = sprintId)
            .map { it.toCommonTask(CommonTaskType.Issue) }

    override suspend fun createSprint(name: String, start: LocalDate, end: LocalDate) =
        sprintApi.createSprint(CreateSprintRequest(name, start, end, currentProjectId))

    override suspend fun editSprint(
        sprintId: Long,
        name: String,
        start: LocalDate,
        end: LocalDate
    ) = sprintApi.editSprint(
        id = sprintId,
        request = EditSprintRequest(name, start, end)
    )

    override suspend fun deleteSprint(sprintId: Long) {
        sprintApi.deleteSprint(sprintId)
    }
}
