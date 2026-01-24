package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.projects.dto.ProjectMemberDTO
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.feature.users.dto.UserDTO

fun getUserDTO(): UserDTO = UserDTO(
    id = getRandomLong(),
    fullName = getRandomString(),
    photo = getRandomString(),
    bigPhoto = getRandomString(),
    username = getRandomString(),
    name = getRandomString(),
    pk = getRandomLong()
)

fun getUser(): User = User(
    id = getRandomLong(),
    fullName = getRandomString(),
    photo = getRandomString(),
    bigPhoto = getRandomString(),
    username = getRandomString(),
    name = getRandomString(),
    pk = getRandomLong()
)

fun getTeamMember(
    id: Long = getRandomLong(),
    name: String = getRandomString(),
    avatarUrl: String? = getRandomString()
): TeamMember = TeamMember(
    id = id,
    avatarUrl = avatarUrl,
    name = name,
    role = getRandomString(),
    username = getRandomString(),
    totalPower = getRandomInt()
)

fun getProjectMemberDTO(): ProjectMemberDTO = ProjectMemberDTO(
    id = getRandomLong(),
    photo = getRandomString(),
    fullNameDisplay = getRandomString(),
    roleName = getRandomString(),
    username = getRandomString()
)
