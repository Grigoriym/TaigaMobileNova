package com.grappim.taigamobile.feature.login.data.api

import com.grappim.taigamobile.feature.login.data.model.AuthRequest
import com.grappim.taigamobile.feature.login.data.model.AuthResponse
import com.grappim.taigamobile.feature.login.data.model.RefreshTokenRequest
import com.grappim.taigamobile.feature.login.data.model.RefreshTokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth")
    suspend fun auth(@Body authRequest: AuthRequest): AuthResponse

    /**
     * Since we call it from the authenticator, which is not coroutine-based, no need to use suspend
     */
    @POST("auth/refresh")
    fun refresh(@Body request: RefreshTokenRequest): Call<RefreshTokenResponse>
}
