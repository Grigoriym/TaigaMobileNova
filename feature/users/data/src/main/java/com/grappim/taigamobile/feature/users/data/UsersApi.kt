package com.grappim.taigamobile.feature.users.data

import com.grappim.taigamobile.feature.users.dto.MemberStatsResponseDTO
import com.grappim.taigamobile.feature.users.dto.StatsDTO
import com.grappim.taigamobile.feature.users.dto.UserDTO
import retrofit2.http.GET
import retrofit2.http.Path

interface UsersApi {

    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: Long): UserDTO

    @GET("users/me")
    suspend fun getMyProfile(): UserDTO

    @GET("users/{id}/stats")
    suspend fun getUserStats(@Path("id") userId: Long): StatsDTO

    @GET("projects/{id}/member_stats")
    suspend fun getMemberStats(@Path("id") projectId: Long): MemberStatsResponseDTO
}
