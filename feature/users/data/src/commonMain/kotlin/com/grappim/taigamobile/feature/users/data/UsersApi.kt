package com.grappim.taigamobile.feature.users.data

import com.grappim.taigamobile.feature.users.dto.MemberStatsResponseDTO
import com.grappim.taigamobile.feature.users.dto.StatsDTO
import com.grappim.taigamobile.feature.users.dto.UserDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.koin.core.annotation.Single

@Single
class UsersApi(private val httpClient: HttpClient) {

    suspend fun getUser(userId: Long): UserDTO =
        httpClient.get("users/$userId").body()

    suspend fun getMyProfile(): UserDTO =
        httpClient.get("users/me").body()

    suspend fun getUserStats(userId: Long): StatsDTO =
        httpClient.get("users/$userId/stats").body()

    suspend fun getMemberStats(projectId: Long): MemberStatsResponseDTO =
        httpClient.get("projects/$projectId/member_stats").body()
}
