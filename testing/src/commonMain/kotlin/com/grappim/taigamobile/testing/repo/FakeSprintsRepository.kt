package com.grappim.taigamobile.testing.repo

import androidx.paging.PagingData
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.feature.sprint.domain.SprintData
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

class FakeSprintsRepository: SprintsRepository {
    override suspend fun getSprintData(sprintId: Long): Result<SprintData> {
        TODO("Not yet implemented")
    }

    override fun getSprintsPaging(isClosed: Boolean): Flow<PagingData<Sprint>> {
        TODO("Not yet implemented")
    }

    override suspend fun getSprints(isClosed: Boolean): ImmutableList<Sprint> {
        TODO("Not yet implemented")
    }

    override suspend fun getSprint(sprintId: Long): Sprint {
        TODO("Not yet implemented")
    }

    override suspend fun getSprintIssues(sprintId: Long): ImmutableList<WorkItem> {
        TODO("Not yet implemented")
    }

    override suspend fun getSprintUserStories(sprintId: Long): ImmutableList<WorkItem> {
        TODO("Not yet implemented")
    }

    override suspend fun getSprintTasks(sprintId: Long): ImmutableList<WorkItem> {
        TODO("Not yet implemented")
    }

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
        TODO("Not yet implemented")
    }
}
