package io.eugenethedev.taigamobile.manager

import io.eugenethedev.taigamobile.testdata.Epic
import io.eugenethedev.taigamobile.testdata.Issue
import io.eugenethedev.taigamobile.testdata.Project
import io.eugenethedev.taigamobile.testdata.Sprint
import io.eugenethedev.taigamobile.testdata.User
import io.eugenethedev.taigamobile.testdata.UserStory

class UserData(
    val id: Long,
    val accessToken: String,
    val refreshToken: String
)

class UserInfo(
    val user: User,
    val data: UserData,
    val projects: Map<Long, Project>
)

class ProjectData(
    val id: Long
) {
    lateinit var epicToId: Map<Epic, Long>
    lateinit var sprintToId: Map<Sprint, Long>
    lateinit var userstoryToId: Map<UserStory, Long>
    lateinit var issueToId: Map<Issue, Long>
}
