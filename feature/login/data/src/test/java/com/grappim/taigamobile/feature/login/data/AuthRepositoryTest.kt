package com.grappim.taigamobile.feature.login.data

import com.grappim.taigamobile.core.storage.AuthStorage
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
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
    private val serverStorage = mockk<ServerStorage>()

    private val taigaSessionStorage = mockk<TaigaSessionStorage>()
    private val authStorage = mockk<AuthStorage>()

    private val sut: AuthRepository =
        AuthRepositoryImpl(
            authApi = authApi,
            taigaSessionStorage = taigaSessionStorage,
            serverStorage = serverStorage,
            authStorage = authStorage,
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
            authStorage.setAuthCredentials(
                token = response.authToken,
                refreshToken = response.refresh
            )
        } just Runs

        coEvery { taigaSessionStorage.setUserId(response.id) } just Runs

        val actual = sut.auth(authData)

        assertTrue(actual.isSuccess)

        verify { serverStorage.defineServer(authData.taigaServer) }
        verify {
            authStorage.setAuthCredentials(
                token = response.authToken,
                refreshToken = response.refresh
            )
        }
        coVerify { taigaSessionStorage.setUserId(response.id) }
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
            authStorage.setAuthCredentials(
                token = response.authToken,
                refreshToken = response.refresh
            )
        }
        coVerify(exactly = 0) { taigaSessionStorage.setUserId(response.id) }
    }
}
