package com.grappim.taigamobile.repositories

import com.grappim.taigamobile.login.data.AuthRepository
import com.grappim.taigamobile.domain.entities.AuthType
import com.grappim.taigamobile.login.domain.IAuthRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AuthRepositoryTest : BaseRepositoryTest() {
    lateinit var authRepository: IAuthRepository

    @BeforeTest
    fun setupAuthRepositoryTest() {
        authRepository = AuthRepository(mockTaigaApi, mockSession)
    }

    @Test
    fun `test basic auth`() = runBlocking {
        val serverUrl = mockSession.serverFlow.value
        mockSession.reset()

        assertFalse(mockSession.isLogged.value)

        with(activeUser) {
            authRepository.auth(serverUrl, AuthType.NORMAL, user.password, user.username)
            assertEquals(serverUrl, mockSession.serverFlow.value)
            assertEquals(data.id, mockSession.currentUserId.value)
        }
    }

    @Test
    fun `test refresh auth token`() = runBlocking {
        mockSession.changeAuthCredentials(
            "wrong token",
            mockSession.refreshToken.value
        ) // simulate token expiration (token is not valid anymore)
        mockTaigaApi.getProject(activeUser.projects.keys.first()) // successful response (because refresh happens)
        return@runBlocking
    }
}
