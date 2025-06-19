package com.grappim.taigamobile.feature.login.data.api

import com.grappim.taigamobile.core.api.RequestWithoutAuthToken
import com.grappim.taigamobile.feature.login.data.model.AuthRequest
import com.grappim.taigamobile.feature.login.data.model.AuthResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth")
    @RequestWithoutAuthToken
    suspend fun auth(@Body authRequest: AuthRequest): AuthResponse
}
