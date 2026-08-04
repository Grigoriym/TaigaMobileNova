package com.grappim.taigamobile.testing.repo

import com.grappim.taigamobile.feature.tasks.domain.Task
import com.grappim.taigamobile.feature.tasks.domain.TasksRepository
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import kotlinx.collections.immutable.ImmutableList

class FakeTasksRepository : TasksRepository {

    var getTaskResult: Task? = null
    var getTaskThrows: Throwable? = null

    var deleteTaskCalled = false
    var deleteTaskThrows: Throwable? = null

    override suspend fun getUserStoryTasks(storyId: Long): ImmutableList<Task> =
        error("not used in this test")

    override suspend fun getTasks(
        assignedId: Long?,
        isClosed: Boolean?,
        watcherId: Long?,
        userStory: Any?,
        project: Long?,
        sprint: Long?
    ): ImmutableList<Task> = error("not used in this test")

    override suspend fun getTask(id: Long): Task {
        getTaskThrows?.let { throw it }
        return getTaskResult ?: error("getTaskResult not set")
    }

    override suspend fun deleteTask(id: Long) {
        deleteTaskCalled = true
        deleteTaskThrows?.let { throw it }
    }

    data class CreateTaskCall(
        val title: String,
        val description: String,
        val parentId: Long?,
        val sprintId: Long?
    )

    var createTaskResult: WorkItem? = null
    var createTaskThrows: Throwable? = null
    val createTaskCalls: MutableList<CreateTaskCall> = mutableListOf()

    override suspend fun createTask(
        title: String,
        description: String,
        parentId: Long?,
        sprintId: Long?
    ): WorkItem {
        createTaskCalls += CreateTaskCall(
            title = title,
            description = description,
            parentId = parentId,
            sprintId = sprintId
        )
        createTaskThrows?.let { throw it }
        return createTaskResult ?: error("createTaskResult not set")
    }
}
