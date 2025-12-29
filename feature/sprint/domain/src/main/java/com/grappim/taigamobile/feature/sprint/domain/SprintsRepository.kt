package com.grappim.taigamobile.feature.sprint.domain

import androidx.paging.PagingData
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface SprintsRepository {
    suspend fun getSprintData(sprintId: Long): Result<SprintData>
    fun getSprints(isClosed: Boolean = false): Flow<PagingData<Sprint>>

    suspend fun getSprints(page: Int, isClosed: Boolean = false): ImmutableList<Sprint>
    suspend fun getSprint(sprintId: Long): Sprint

    suspend fun getSprintIssues(sprintId: Long): ImmutableList<WorkItem>
    suspend fun getSprintUserStories(sprintId: Long): ImmutableList<WorkItem>
    suspend fun getSprintTasks(sprintId: Long): ImmutableList<WorkItem>

    suspend fun createSprint(name: String, start: LocalDate, end: LocalDate)
    suspend fun editSprint(sprintId: Long, name: String, start: LocalDate, end: LocalDate)
    suspend fun deleteSprint(sprintId: Long)
}
