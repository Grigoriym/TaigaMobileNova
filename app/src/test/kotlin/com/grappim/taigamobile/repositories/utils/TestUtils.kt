package com.grappim.taigamobile.repositories.utils

import com.grappim.taigamobile.testdata.Task
import com.grappim.taigamobile.testdata.TestData

class TestCommonTask(
    val title: String,
    val assigneeFullName: String? = null,
    val isClosed: Boolean
)

fun getTestTasks(projectId: Int): List<Task> {
    val fromSprints = TestData.projects[projectId].sprints.flatMap { it.tasks }
    val fromUserStories = TestData.projects[projectId].userstories.flatMap { it.tasks }

    return fromSprints + fromUserStories
}