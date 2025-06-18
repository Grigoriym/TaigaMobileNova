package com.grappim.taigamobile.sprint

import androidx.paging.PagingData
import com.grappim.taigamobile.domain.entities.CommonTask
import com.grappim.taigamobile.domain.entities.Sprint
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ISprintsRepository {
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
