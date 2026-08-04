package com.grappim.taigamobile.testing.repo

import androidx.paging.PagingData
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.feature.sprint.domain.SprintData
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

class FakeSprintsRepository: SprintsRepository {
    var getSprintDataResult: Result<SprintData> = Result.failure(Exception("getSprintDataResult not set"))
    var deleteSprintThrows: Exception? = null
    var deleteSprintCalled = false

    override suspend fun getSprintData(sprintId: Long): Result<SprintData> = getSprintDataResult

    override fun getSprintsPaging(isClosed: Boolean): Flow<PagingData<Sprint>> =
        flowOf(PagingData.empty())

    var getSprintsResult: ImmutableList<Sprint> = persistentListOf()
    var getSprintsThrows: Throwable? = null
    var getSprintsIsClosed: Boolean? = null

    override suspend fun getSprints(isClosed: Boolean): ImmutableList<Sprint> {
        getSprintsIsClosed = isClosed
        getSprintsThrows?.let { throw it }
        return getSprintsResult
    }

    var getSprintResult: Sprint? = null

    override suspend fun getSprint(sprintId: Long): Sprint =
        getSprintResult ?: error("getSprintResult not set")

    override suspend fun getSprintIssues(sprintId: Long): ImmutableList<WorkItem> =
        error("not used in this test")

    override suspend fun getSprintUserStories(sprintId: Long): ImmutableList<WorkItem> =
        error("not used in this test")

    override suspend fun getSprintTasks(sprintId: Long): ImmutableList<WorkItem> =
        error("not used in this test")

    override suspend fun createSprint(
        name: String,
        start: LocalDate,
        end: LocalDate
    ) {
    }

    override suspend fun editSprint(
        sprintId: Long,
        name: String,
        start: LocalDate,
        end: LocalDate
    ) {
    }

    override suspend fun deleteSprint(sprintId: Long) {
        deleteSprintCalled = true
        deleteSprintThrows?.let { throw it }
    }
}
