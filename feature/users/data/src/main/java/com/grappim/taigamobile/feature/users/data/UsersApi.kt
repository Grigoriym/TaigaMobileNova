package com.grappim.taigamobile.feature.users.data

import com.grappim.taigamobile.core.domain.Stats
import com.grappim.taigamobile.core.domain.User
import retrofit2.http.GET
import retrofit2.http.Path

interface UsersApi {

    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: Long): User

    @GET("users/me")
    suspend fun getMyProfile(): User

    @GET("users/{id}/stats")
    suspend fun getUserStats(@Path("id") userId: Long): Stats

    @GET("projects/{id}/member_stats")
    suspend fun getMemberStats(@Path("id") projectId: Long): MemberStatsResponse
}
