package com.grappim.taigamobile.feature.login.data

import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.login.data.api.AuthApi
import com.grappim.taigamobile.feature.login.data.model.AuthRequest
import com.grappim.taigamobile.feature.login.data.model.AuthResponse
import com.grappim.taigamobile.feature.login.data.repo.AuthRepositoryImpl
import com.grappim.taigamobile.feature.login.domain.model.AuthData
import com.grappim.taigamobile.feature.login.domain.model.AuthType
import com.grappim.taigamobile.feature.login.domain.repo.AuthRepository
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.testException
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class AuthRepositoryTest {

    private val authApi = mockk<AuthApi>()
    private val session = mockk<Session>()
    private val serverStorage = mockk<ServerStorage>()

    private val sut: AuthRepository =
        AuthRepositoryImpl(
            authApi = authApi,
            session = session,
            serverStorage = serverStorage,
            dispatcher = UnconfinedTestDispatcher()
        )

    @Test
    fun `on auth without error then return success`() = runTest {
        val authData = AuthData(
            taigaServer = getRandomString(),
            authType = AuthType.NORMAL,
            password = getRandomString(),
            username = getRandomString()
        )
        val response = AuthResponse(
            authToken = getRandomString(),
            refresh = getRandomString(),
            id = getRandomLong()
        )

        every { serverStorage.defineServer(authData.taigaServer) } just Runs
        coEvery {
            authApi.auth(
                AuthRequest(
                    username = authData.username,
                    password = authData.password,
                    type = authData.authType.value
                )
            )
        } returns response
        every {
            session.changeAuthCredentials(
                token = response.authToken,
                refreshToken = response.refresh ?: "missing"
            )
        } just Runs

        every { session.changeCurrentUserId(response.id) } just Runs

        val actual = sut.auth(authData)

        assertTrue(actual.isSuccess)

        verify { serverStorage.defineServer(authData.taigaServer) }
        verify {
            session.changeAuthCredentials(
                token = response.authToken,
                refreshToken = response.refresh ?: "missing"
            )
        }
        verify { session.changeCurrentUserId(response.id) }
        coVerify {
            authApi.auth(
                AuthRequest(
                    username = authData.username,
                    password = authData.password,
                    type = authData.authType.value
                )
            )
        }
    }

    @Test
    fun `on auth with error then return failure`() = runTest {
        val authData = AuthData(
            taigaServer = getRandomString(),
            authType = AuthType.NORMAL,
            password = getRandomString(),
            username = getRandomString()
        )
        val response = AuthResponse(
            authToken = getRandomString(),
            refresh = getRandomString(),
            id = getRandomLong()
        )

        every { serverStorage.defineServer(authData.taigaServer) } just Runs
        coEvery {
            authApi.auth(
                AuthRequest(
                    username = authData.username,
                    password = authData.password,
                    type = authData.authType.value
                )
            )
        } throws testException

        val actual = sut.auth(authData)

        assertTrue(actual.isFailure)

        verify { serverStorage.defineServer(authData.taigaServer) }
        coVerify {
            authApi.auth(
                AuthRequest(
                    username = authData.username,
                    password = authData.password,
                    type = authData.authType.value
                )
            )
        }
        verify(exactly = 0) {
            session.changeAuthCredentials(
                token = response.authToken,
                refreshToken = response.refresh ?: "missing"
            )
        }
        verify(exactly = 0) { session.changeCurrentUserId(response.id) }
    }
}
