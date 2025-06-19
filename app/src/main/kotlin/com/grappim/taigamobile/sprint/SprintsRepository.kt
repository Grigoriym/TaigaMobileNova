package com.grappim.taigamobile.sprint

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.api.handle404
import com.grappim.taigamobile.core.api.toCommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.Sprint
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.data.api.TaigaApi
import com.grappim.taigamobile.feature.sprint.data.CreateSprintRequest
import com.grappim.taigamobile.feature.sprint.data.EditSprintRequest
import com.grappim.taigamobile.feature.sprint.data.SprintApi
import com.grappim.taigamobile.feature.sprint.data.SprintPagingSource
import com.grappim.taigamobile.feature.sprint.data.toSprint
import com.grappim.taigamobile.feature.sprint.domain.ISprintsRepository
import com.grappim.taigamobile.feature.userstories.data.UserStoriesApi
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class SprintsRepository @Inject constructor(
    private val taigaApi: TaigaApi,
    private val session: Session,
    private val userStoriesApi: UserStoriesApi,
    private val sprintApi: SprintApi,
    private val issuesApi: com.grappim.taigamobile.feature.issues.data.IssuesApi
) : ISprintsRepository {
    override fun getSprints(isClosed: Boolean): Flow<PagingData<Sprint>> = Pager(
        PagingConfig(
            pageSize = 20,
            enablePlaceholders = false
        )
    ) {
        SprintPagingSource(sprintApi, isClosed, session)
    }.flow

    private val currentProjectId get() = session.currentProjectId.value

    override suspend fun getSprintUserStories(sprintId: Long) =
        userStoriesApi.getUserStories(project = currentProjectId, sprint = sprintId)
            .map { it.toCommonTask(CommonTaskType.UserStory) }

    override suspend fun getSprints(page: Int, isClosed: Boolean) = handle404 {
        sprintApi.getSprints(currentProjectId, page, isClosed).map { it.toSprint() }
    }

    override suspend fun getSprint(sprintId: Long) = sprintApi.getSprint(sprintId).toSprint()

    override suspend fun getSprintTasks(sprintId: Long) =
        taigaApi.getTasks(userStory = "null", project = currentProjectId, sprint = sprintId)
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
