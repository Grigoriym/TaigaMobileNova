package com.grappim.taigamobile.feature.tasks.domain

import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import kotlinx.collections.immutable.ImmutableList

interface TasksRepository {
    suspend fun getUserStoryTasks(storyId: Long): ImmutableList<Task>
    suspend fun getTasks(
        assignedId: Long? = null,
        isClosed: Boolean? = null,
        watcherId: Long? = null,
        userStory: Any? = null,
        project: Long? = null,
        sprint: Long? = null
    ): ImmutableList<Task>

    suspend fun getTask(id: Long): Task
    suspend fun deleteTask(id: Long)
    suspend fun createTask(title: String, description: String, parentId: Long?, sprintId: Long?): WorkItem
}
