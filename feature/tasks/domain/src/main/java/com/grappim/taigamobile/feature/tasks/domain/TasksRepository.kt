package com.grappim.taigamobile.feature.tasks.domain

import com.grappim.taigamobile.core.domain.CommonTask

interface TasksRepository {
    suspend fun getUserStoryTasks(storyId: Long): List<CommonTask>
    suspend fun getTasks(
        assignedId: Long? = null,
        isClosed: Boolean? = null,
        watcherId: Long? = null,
        userStory: Any? = null,
        project: Long? = null,
        sprint: Long? = null
    ): List<CommonTask>
    suspend fun getTask(id: Long): Task
    suspend fun deleteTask(id: Long)
}
