package com.grappim.taigamobile.feature.tasks.domain

import com.grappim.taigamobile.core.domain.CommonTask

interface TasksRepository {
    suspend fun getUserStoryTasks(storyId: Long): List<CommonTask>
    suspend fun getTasks(
        assignedId: Long? = null,
        isClosed: Boolean? = null,
        watcherId: Long? = null
    ): List<CommonTask>
}
