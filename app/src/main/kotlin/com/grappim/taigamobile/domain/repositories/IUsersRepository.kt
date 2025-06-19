package com.grappim.taigamobile.domain.repositories

import com.grappim.taigamobile.core.domain.Stats
import com.grappim.taigamobile.core.domain.TeamMember
import com.grappim.taigamobile.core.domain.User

interface IUsersRepository {
    suspend fun getMe(): User
    suspend fun getUser(userId: Long): User
    suspend fun getUserStats(userId: Long): Stats
    suspend fun getTeam(): List<TeamMember>
}
