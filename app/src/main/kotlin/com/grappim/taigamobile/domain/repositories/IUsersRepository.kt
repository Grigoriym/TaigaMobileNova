package com.grappim.taigamobile.domain.repositories

import com.grappim.taigamobile.domain.entities.Stats
import com.grappim.taigamobile.domain.entities.TeamMember
import com.grappim.taigamobile.domain.entities.User

interface IUsersRepository {
    suspend fun getMe(): User
    suspend fun getUser(userId: Long): User
    suspend fun getUserStats(userId: Long): Stats
    suspend fun getTeam(): List<TeamMember>
}