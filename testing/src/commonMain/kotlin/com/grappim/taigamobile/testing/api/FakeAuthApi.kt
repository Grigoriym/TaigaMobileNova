package com.grappim.taigamobile.testing.api

import com.grappim.taigamobile.feature.login.data.AuthApi
import com.grappim.taigamobile.feature.login.dto.AuthRequest
import com.grappim.taigamobile.feature.login.dto.AuthResponse
import com.grappim.taigamobile.feature.login.dto.RefreshTokenRequest
import com.grappim.taigamobile.feature.login.dto.RefreshTokenResponse

class FakeAuthApi: AuthApi {
    override suspend fun auth(authRequest: AuthRequest): AuthResponse {
        TODO("Not yet implemented")
    }

    override suspend fun refresh(request: RefreshTokenRequest): RefreshTokenResponse {
        TODO("Not yet implemented")
    }
}
