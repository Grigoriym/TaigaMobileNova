package com.grappim.taigamobile.testing.api

import com.grappim.taigamobile.feature.users.data.UsersApi
import com.grappim.taigamobile.feature.users.dto.MemberStatsResponseDTO
import com.grappim.taigamobile.feature.users.dto.StatsDTO
import com.grappim.taigamobile.feature.users.dto.UserDTO
import com.grappim.taigamobile.testing.models.getStatsDTO
import com.grappim.taigamobile.testing.models.getUserDTO

class FakeUsersApi : UsersApi {
    var myProfile: UserDTO = getUserDTO()
    val userById: MutableMap<Long, UserDTO> = mutableMapOf()
    var statsDTO: StatsDTO = getStatsDTO()
    var memberStatsResponseDTO: MemberStatsResponseDTO = MemberStatsResponseDTO(
        closedBugs = emptyMap(),
        closedTasks = emptyMap(),
        createdBugs = emptyMap(),
        iocaineTasks = emptyMap(),
        wikiChanges = emptyMap()
    )

    override suspend fun getUser(userId: Long): UserDTO =
        userById[userId] ?: error("no user configured for id $userId")

    override suspend fun getMyProfile(): UserDTO = myProfile

    override suspend fun getUserStats(userId: Long): StatsDTO = statsDTO

    override suspend fun getMemberStats(projectId: Long): MemberStatsResponseDTO = memberStatsResponseDTO
}
