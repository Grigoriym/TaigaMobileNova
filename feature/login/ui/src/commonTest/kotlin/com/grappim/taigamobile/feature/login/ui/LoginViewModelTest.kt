@file:OptIn(ExperimentalCoroutinesApi::class)

package com.grappim.taigamobile.feature.login.ui

import app.cash.turbine.test
import com.grappim.taigamobile.feature.login.domain.model.AuthData
import com.grappim.taigamobile.feature.login.domain.model.AuthType
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.repo.FakeAuthRepository
import com.grappim.taigamobile.testing.storage.FakeServerStorage
import com.grappim.taigamobile.testing.utils.getRandomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LoginViewModelTest {

    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: LoginViewModel

    private val authRepository = FakeAuthRepository()
    private val defaultServer = getRandomString()
    private val serverStorage = FakeServerStorage(defaultServer)

    private val correctServer = "https://10.0.2.2:9000"

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
        sut = LoginViewModel(authRepository, serverStorage)
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    @Test
    fun `on onActionDialogConfirm without error should login`() = runTest {
        val server = getRandomString()
        val authType = AuthType.LDAP
        val password = getRandomString()
        val username = getRandomString()

        val authData = AuthData(server, authType, password, username)

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

            assertEquals(authData, authRepository.authCalledWith)
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

        assertEquals(0, authRepository.authCallCount)
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

        assertEquals(0, authRepository.authCallCount)
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

        assertEquals(0, authRepository.authCallCount)
    }

    @Test
    fun `on validateAuthData with valid data but without https in server should not login`() {
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

        assertEquals(0, authRepository.authCallCount)
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
    fun `on onGithubLoginClick with invalid server should set server error`() {
        sut.state.value.onServerValueChange(getRandomString())

        sut.state.value.onGithubLoginClick()

        assertTrue(sut.state.value.isServerInputError)
    }

    @Test
    fun `on onGithubLoginClick with http server should show alert`() {
        sut.state.value.onServerValueChange("http://10.0.2.2:9000")

        sut.state.value.onGithubLoginClick()

        assertFalse(sut.state.value.isServerInputError)
        assertTrue(sut.state.value.isAlertVisible)
    }

    @Test
    fun `on onGithubLoginClick with valid server should emit webview url with client id`() = runTest {
        val clientId = getRandomString()
        authRepository.githubClientIdResult = Result.success(clientId)
        sut.state.value.onServerValueChange(correctServer)

        sut.openGithubWebView.test {
            sut.state.value.onGithubLoginClick()
            val url = awaitItem()
            assertTrue(url.contains(clientId))
        }
    }

    @Test
    fun `on github oauth success should emit login successful`() = runTest {
        val code = getRandomString()
        val clientId = getRandomString()
        authRepository.githubClientIdResult = Result.success(clientId)
        authRepository.authWithGithubResult = Result.success(Unit)
        sut.state.value.onServerValueChange(correctServer)

        sut.loginSuccessful.test {
            sut.state.value.onGithubLoginClick()
            sut.onGithubCodeReceived(code)
            assertTrue(awaitItem())
            assertEquals(code, authRepository.authWithGithubCalls.single())
        }
    }

    @Test
    fun `on github oauth auth failure should update error state`() = runTest {
        val clientId = getRandomString()
        authRepository.githubClientIdResult = Result.success(clientId)
        authRepository.authWithGithubResult = Result.failure(RuntimeException("auth failed"))
        sut.state.value.onServerValueChange(correctServer)

        sut.state.value.onGithubLoginClick()
        sut.onGithubCodeReceived(getRandomString())

        assertFalse(sut.state.value.error.isEmpty())
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
