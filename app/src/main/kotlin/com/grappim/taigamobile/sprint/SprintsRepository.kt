package com.grappim.taigamobile.sprint

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.data.api.CreateSprintRequest
import com.grappim.taigamobile.data.api.EditSprintRequest
import com.grappim.taigamobile.data.api.TaigaApi
import com.grappim.taigamobile.data.repositories.handle404
import com.grappim.taigamobile.data.repositories.toCommonTask
import com.grappim.taigamobile.data.repositories.toSprint
import com.grappim.taigamobile.data.repositories.withIO
import com.grappim.taigamobile.domain.entities.CommonTaskType
import com.grappim.taigamobile.domain.entities.Sprint
import com.grappim.taigamobile.issues.IssuesApi
import com.grappim.taigamobile.state.Session
import com.grappim.taigamobile.userstories.UserStoriesApi
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class SprintsRepository @Inject constructor(
    private val taigaApi: TaigaApi,
    private val session: Session,
    private val userStoriesApi: UserStoriesApi,
    private val sprintApi: SprintApi,
    private val issuesApi: IssuesApi
) : ISprintsRepository {
    override fun getSprints(isClosed: Boolean): Flow<PagingData<Sprint>> =
        Pager(
            PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            )
        ) {
            SprintPagingSource(sprintApi, isClosed, session)
        }.flow

    private val currentProjectId get() = session.currentProjectId.value

    override suspend fun getSprintUserStories(sprintId: Long) = withIO {
        userStoriesApi.getUserStories(project = currentProjectId, sprint = sprintId)
            .map { it.toCommonTask(CommonTaskType.UserStory) }
    }

    override suspend fun getSprints(page: Int, isClosed: Boolean) = withIO {
        handle404 {
            sprintApi.getSprints(currentProjectId, page, isClosed).map { it.toSprint() }
        }
    }

    override suspend fun getSprint(sprintId: Long) = withIO {
        sprintApi.getSprint(sprintId).toSprint()
    }

    override suspend fun getSprintTasks(sprintId: Long) = withIO {
        taigaApi.getTasks(userStory = "null", project = currentProjectId, sprint = sprintId)
            .map { it.toCommonTask(CommonTaskType.Task) }
    }

    override suspend fun getSprintIssues(sprintId: Long) = withIO {
        issuesApi.getIssues(project = currentProjectId, sprint = sprintId)
            .map { it.toCommonTask(CommonTaskType.Issue) }
    }

    override suspend fun createSprint(name: String, start: LocalDate, end: LocalDate) = withIO {
        sprintApi.createSprint(CreateSprintRequest(name, start, end, currentProjectId))
    }

    override suspend fun editSprint(
        sprintId: Long,
        name: String,
        start: LocalDate,
        end: LocalDate
    ) = withIO {
        sprintApi.editSprint(
            id = sprintId,
            request = EditSprintRequest(name, start, end)
        )
    }

    override suspend fun deleteSprint(sprintId: Long) = withIO {
        sprintApi.deleteSprint(sprintId)
        return@withIO
    }
}
