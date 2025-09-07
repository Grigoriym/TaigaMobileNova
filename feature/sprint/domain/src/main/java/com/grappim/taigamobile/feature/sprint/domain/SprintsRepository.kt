package com.grappim.taigamobile.feature.sprint.domain

import androidx.paging.PagingData
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.Sprint
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface SprintsRepository {
    suspend fun getSprintData(sprintId: Long): Result<SprintData>
    fun getSprints(isClosed: Boolean = false): Flow<PagingData<Sprint>>

    suspend fun getSprints(page: Int, isClosed: Boolean = false): List<Sprint>
    suspend fun getSprint(sprintId: Long): Sprint

    suspend fun getSprintIssues(sprintId: Long): List<CommonTask>
    suspend fun getSprintUserStories(sprintId: Long): List<CommonTask>
    suspend fun getSprintTasks(sprintId: Long): List<CommonTask>

    suspend fun createSprint(name: String, start: LocalDate, end: LocalDate)
    suspend fun editSprint(sprintId: Long, name: String, start: LocalDate, end: LocalDate)
    suspend fun deleteSprint(sprintId: Long)
}
