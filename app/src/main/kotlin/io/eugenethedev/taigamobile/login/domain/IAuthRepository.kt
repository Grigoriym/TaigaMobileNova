package io.eugenethedev.taigamobile.login.domain

interface IAuthRepository {
    suspend fun auth(authData: AuthData): Result<Unit>
}
