package com.grappim.taigamobile.feature.users.data

import com.grappim.taigamobile.feature.users.dto.MemberStatsResponseDTO
import com.grappim.taigamobile.feature.users.dto.StatsDTO
import com.grappim.taigamobile.feature.users.dto.UserDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.koin.core.annotation.Single

interface UsersApi {
    suspend fun getUser(userId: Long): UserDTO
    suspend fun getMyProfile(): UserDTO
    suspend fun getUserStats(userId: Long): StatsDTO
    suspend fun getMemberStats(projectId: Long): MemberStatsResponseDTO
}

@Single(binds = [UsersApi::class])
class UsersApiImpl(private val httpClient: HttpClient) : UsersApi {

    override suspend fun getUser(userId: Long): UserDTO = httpClient.get("users/$userId").body()

    override suspend fun getMyProfile(): UserDTO = httpClient.get("users/me").body()

    override suspend fun getUserStats(userId: Long): StatsDTO = httpClient.get("users/$userId/stats").body()

    override suspend fun getMemberStats(projectId: Long): MemberStatsResponseDTO =
        httpClient.get("projects/$projectId/member_stats").body()
}
