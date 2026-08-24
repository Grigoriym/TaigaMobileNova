package com.grappim.taigamobile.feature.login.data

import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.login.domain.model.AuthData
import com.grappim.taigamobile.feature.login.domain.model.AuthType
import com.grappim.taigamobile.feature.login.domain.repo.AuthRepository
import com.grappim.taigamobile.feature.login.dto.AuthResponse
import com.grappim.taigamobile.feature.login.dto.TaigaConfJson
import com.grappim.taigamobile.testing.api.FakeAuthApi
import com.grappim.taigamobile.testing.storage.FakeAuthStorage
import com.grappim.taigamobile.testing.storage.FakeServerStorage
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class AuthRepositoryTest {

    private val authApi = FakeAuthApi()
    private val serverStorage: ServerStorage = FakeServerStorage()

    private val taigaSessionStorage = FakeTaigaSessionStorage()
    private val authStorage = FakeAuthStorage()

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

        authApi.authResult = response

        val actual = sut.auth(authData)

        val call = authApi.authCalls.single()

        assertEquals(authData.password, call.password)
        assertEquals(authData.username, call.username)
        assertEquals(authData.authType.value, call.type)

        assertTrue(actual.isSuccess)
    }

    @Test
    fun `on auth with error then return failure`() = runTest {
        val authData = AuthData(
            taigaServer = getRandomString(),
            authType = AuthType.NORMAL,
            password = getRandomString(),
            username = getRandomString()
        )
        val actual = sut.auth(authData)
        assertTrue(actual.isFailure)
    }

    @Test
    fun `on authWithGithub without error then return success and store credentials`() = runTest {
        val code = getRandomString()
        val response = AuthResponse(
            authToken = getRandomString(),
            refresh = getRandomString(),
            id = getRandomLong()
        )
        authApi.githubAuthResult = response

        val actual = sut.authWithGithub(code)

        val call = authApi.githubAuthCalls.single()
        assertEquals(code, call.code)
        assertEquals(AuthType.GITHUB.value, call.type)
        assertEquals(response.authToken, authStorage.setCredentialsToken)
        assertEquals(response.refresh, authStorage.setCredentialsRefreshToken)
        assertTrue(actual.isSuccess)
    }

    @Test
    fun `on authWithGithub with error then return failure`() = runTest {
        val actual = sut.authWithGithub(getRandomString())
        assertTrue(actual.isFailure)
    }

    @Test
    fun `on getGithubClientId without error then return success with client id`() = runTest {
        val clientId = getRandomString()
        authApi.confJsonResult = TaigaConfJson(gitHubClientId = clientId)

        val actual = sut.getGithubClientId("https://example.com/")

        assertEquals(clientId, actual.getOrNull())
        assertEquals("https://example.com", authApi.confJsonCalls.single())
        assertEquals("https://example.com", serverStorage.server)
    }

    @Test
    fun `on getGithubClientId when server has no github client id then return failure`() = runTest {
        authApi.confJsonResult = TaigaConfJson(gitHubClientId = null)

        val actual = sut.getGithubClientId(getRandomString())

        assertTrue(actual.isFailure)
        assertEquals("GitHub auth is not configured on this server", actual.exceptionOrNull()?.message)
    }

    @Test
    fun `on getGithubClientId when api throws then return failure`() = runTest {
        authApi.confJsonThrows = testException

        val actual = sut.getGithubClientId(getRandomString())

        assertTrue(actual.isFailure)
        assertEquals(testException.message, actual.exceptionOrNull()?.message)
    }
}
