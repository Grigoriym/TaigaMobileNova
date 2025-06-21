package com.grappim.taigamobile.feature.login.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.text.input.TextFieldValue
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

    @get:Rule
    var instantRule = InstantTaskExecutorRule()

    private lateinit var sut: LoginViewModel

    private val authRepository = mockk<AuthRepository>()
    private val serverStorage = mockk<ServerStorage>()

    private val server = getRandomString()

    @Before
    fun setup() {
        every { serverStorage.server } returns server

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

        sut.state.value.onServerValueChange(TextFieldValue(server))
        sut.state.value.onAuthTypeChange(authType)
        sut.state.value.onPasswordValueChange(TextFieldValue(password))
        sut.state.value.onLoginValueChange(TextFieldValue(username))

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
    fun `on setPassword should change the password`() {
        assertFalse(sut.state.value.isPasswordInputError)
        assertEquals("", sut.state.value.password.text)

        sut.state.value.onPasswordValueChange(TextFieldValue("password"))

        assertEquals("password", sut.state.value.password.text)
        assertFalse(sut.state.value.isPasswordInputError)
    }

    @Test
    fun `on setLogin should change the login`() {
        assertFalse(sut.state.value.isLoginInputError)
        assertEquals("", sut.state.value.login.text)

        sut.state.value.onLoginValueChange(TextFieldValue("login"))

        assertEquals("login", sut.state.value.login.text)
        assertFalse(sut.state.value.isLoginInputError)
    }

    @Test
    fun `on setServer should change the server`() {
        val newServerValue = getRandomString()

        assertFalse(sut.state.value.isServerInputError)
        assertEquals(server, sut.state.value.server.text)

        sut.state.value.onServerValueChange(TextFieldValue(newServerValue))

        assertEquals(newServerValue, sut.state.value.server.text)
        assertFalse(sut.state.value.isServerInputError)
    }
}
