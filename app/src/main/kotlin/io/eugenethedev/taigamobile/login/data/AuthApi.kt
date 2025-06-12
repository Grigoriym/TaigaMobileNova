package io.eugenethedev.taigamobile.login.data

import io.eugenethedev.taigamobile.data.api.AuthRequest
import io.eugenethedev.taigamobile.data.api.AuthResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth")
    @RequestWithoutAuthToken
    suspend fun auth(@Body authRequest: AuthRequest): AuthResponse
}

/**
 * We don't need to provide an auth token for the requests with this annotation, like auth
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequestWithoutAuthToken
