package com.grappim.taigamobile.manager

import com.grappim.taigamobile.testdata.Epic
import com.grappim.taigamobile.testdata.Issue
import com.grappim.taigamobile.testdata.Project
import com.grappim.taigamobile.testdata.Sprint
import com.grappim.taigamobile.testdata.User
import com.grappim.taigamobile.testdata.UserStory

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
