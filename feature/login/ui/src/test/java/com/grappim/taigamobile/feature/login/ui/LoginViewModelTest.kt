package com.grappim.taigamobile.feature.login.ui

import app.cash.turbine.test
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.login.domain.model.AuthData
import com.grappim.taigamobile.feature.login.domain.model.AuthType
import com.grappim.taigamobile.feature.login.domain.repo.AuthRepository
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.getRandomString
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LoginViewModelTest {

    @get:Rule
    val coroutineRule = MainDispatcherRule()

    private lateinit var sut: LoginViewModel

    private val authRepository = mockk<AuthRepository>()
    private val serverStorage = mockk<ServerStorage>()

    private val defaultServer = getRandomString()
    private val correctServer = "https://10.0.2.2:9000"

    @Before
    fun setup() {
        every { serverStorage.server } returns defaultServer

        sut = LoginViewModel(authRepository, serverStorage)
    }

    @Test
    fun `on onActionDialogConfirm without error should login`() = runTest {
        val server = getRandomString()
        val authType = AuthType.LDAP
        val password = getRandomString()
        val username = getRandomString()

        val authData = AuthData(server, authType, password, username)

        coEvery { authRepository.auth(authData) } returns Result.success(Unit)

        sut.state.value.onServerValueChange(server)
        sut.state.value.onAuthTypeChange(authType)
        sut.state.value.onPasswordValueChange(password)
        sut.state.value.onLoginValueChange(username)

        sut.loginSuccessful.test {
            sut.state.value.onActionDialogConfirm()

            assertFalse(sut.state.value.isLoading)
            assertFalse(sut.state.value.isAlertVisible)

            assertTrue(awaitItem())
            assertFalse(sut.state.value.isLoading)

            coVerify { authRepository.auth(authData) }
        }
    }

    @Test
    fun `on validateAuthData with incorrect server should not login`() {
        val authType = AuthType.LDAP
        val password = getRandomString()
        val username = getRandomString()
        val incorrectServer = getRandomString()

        sut.state.value.onServerValueChange(incorrectServer)
        sut.state.value.onAuthTypeChange(authType)
        sut.state.value.onPasswordValueChange(password)
        sut.state.value.onLoginValueChange(username)

        sut.state.value.validateAuthData(authType)

        assertTrue(sut.state.value.isServerInputError)
        assertFalse(sut.state.value.isLoginInputError)
        assertFalse(sut.state.value.isPasswordInputError)

        coVerify(exactly = 0) { authRepository.auth(any()) }
    }

    @Test
    fun `on validateAuthData with empty login should not login`() {
        val authType = AuthType.LDAP
        val password = getRandomString()
        val username = ""

        sut.state.value.onServerValueChange(correctServer)
        sut.state.value.onAuthTypeChange(authType)
        sut.state.value.onPasswordValueChange(password)
        sut.state.value.onLoginValueChange(username)

        sut.state.value.validateAuthData(authType)

        assertFalse(sut.state.value.isServerInputError)
        assertTrue(sut.state.value.isLoginInputError)
        assertFalse(sut.state.value.isPasswordInputError)

        coVerify(exactly = 0) { authRepository.auth(any()) }
    }

    @Test
    fun `on validateAuthData with empty password should not login`() {
        val authType = AuthType.LDAP
        val password = ""
        val username = getRandomString()

        sut.state.value.onServerValueChange(correctServer)
        sut.state.value.onAuthTypeChange(authType)
        sut.state.value.onPasswordValueChange(password)
        sut.state.value.onLoginValueChange(username)

        sut.state.value.validateAuthData(authType)

        assertFalse(sut.state.value.isServerInputError)
        assertFalse(sut.state.value.isLoginInputError)
        assertTrue(sut.state.value.isPasswordInputError)

        coVerify(exactly = 0) { authRepository.auth(any()) }
    }

    @Test
    fun `on validateAuthData with valid data, but without https in server should not login`() {
        val authType = AuthType.LDAP
        val password = getRandomString()
        val username = getRandomString()
        val server = "http://10.0.2.2:9000"

        sut.state.value.onServerValueChange(server)
        sut.state.value.onAuthTypeChange(authType)
        sut.state.value.onPasswordValueChange(password)
        sut.state.value.onLoginValueChange(username)

        sut.state.value.validateAuthData(authType)

        assertFalse(sut.state.value.isServerInputError)
        assertFalse(sut.state.value.isLoginInputError)
        assertFalse(sut.state.value.isPasswordInputError)

        assertTrue(sut.state.value.isAlertVisible)

        coVerify(exactly = 0) { authRepository.auth(any()) }
    }

    @Test
    fun `on onAuthTypeChange should change the authType`() {
        assertEquals(AuthType.NORMAL, sut.state.value.authType)

        sut.state.value.onAuthTypeChange(AuthType.LDAP)

        assertEquals(AuthType.LDAP, sut.state.value.authType)
    }

    @Test
    fun `on setIsAlertVisible should change the isAlertVisible`() {
        assertFalse(sut.state.value.isAlertVisible)

        sut.state.value.setIsAlertVisible(true)

        assertTrue(sut.state.value.isAlertVisible)
    }

    @Test
    fun `on changePasswordVisibility should change the isPasswordVisible`() {
        assertFalse(sut.state.value.isPasswordVisible)

        sut.state.value.setIsPasswordVisible(true)

        assertTrue(sut.state.value.isPasswordVisible)
    }

    @Test
    fun `on setPassword should change the password`() {
        assertFalse(sut.state.value.isPasswordInputError)
        assertEquals("", sut.state.value.password)

        sut.state.value.onPasswordValueChange("password")

        assertEquals("password", sut.state.value.password)
        assertFalse(sut.state.value.isPasswordInputError)
    }

    @Test
    fun `on setLogin should change the login`() {
        assertFalse(sut.state.value.isLoginInputError)
        assertEquals("", sut.state.value.login)

        sut.state.value.onLoginValueChange("login")

        assertEquals("login", sut.state.value.login)
        assertFalse(sut.state.value.isLoginInputError)
    }

    @Test
    fun `on setServer should change the server`() {
        val newServerValue = getRandomString()

        assertFalse(sut.state.value.isServerInputError)
        assertEquals(defaultServer, sut.state.value.server)

        sut.state.value.onServerValueChange(newServerValue)

        assertEquals(newServerValue, sut.state.value.server)
        assertFalse(sut.state.value.isServerInputError)
    }
}
