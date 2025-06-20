package com.grappim.taigamobile.feature.login.ui

import androidx.compose.ui.text.input.TextFieldValue
import com.grappim.taigamobile.core.api.ApiConstants
import com.grappim.taigamobile.feature.login.domain.model.AuthType
import com.grappim.taigamobile.feature.login.domain.repo.AuthRepository
import com.grappim.taigamobile.testing.MainDispatcherRule
import io.mockk.mockk
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

    @Before
    fun setup() {
        sut = LoginViewModel(authRepository)
    }

    @Test
    fun `on onAuthTypeChange should change the authType`() {
        assertEquals(AuthType.NORMAL, sut.loginState.value.authType)

        sut.loginState.value.onAuthTypeChange(AuthType.LDAP)

        assertEquals(AuthType.LDAP, sut.loginState.value.authType)
    }

    @Test
    fun `on setIsAlertVisible should change the isAlertVisible`() {
        assertFalse(sut.loginState.value.isAlertVisible)

        sut.loginState.value.setIsAlertVisible(true)

        assertTrue(sut.loginState.value.isAlertVisible)
    }

    @Test
    fun `on setPassword should change the password`() {
        assertFalse(sut.loginState.value.isPasswordInputError)
        assertEquals("", sut.loginState.value.password.text)

        sut.loginState.value.onPasswordValueChange(TextFieldValue("password"))

        assertEquals("password", sut.loginState.value.password.text)
        assertFalse(sut.loginState.value.isPasswordInputError)
    }

    @Test
    fun `on setLogin should change the login`() {
        assertFalse(sut.loginState.value.isLoginInputError)
        assertEquals("", sut.loginState.value.login.text)

        sut.loginState.value.onLoginValueChange(TextFieldValue("login"))

        assertEquals("login", sut.loginState.value.login.text)
        assertFalse(sut.loginState.value.isLoginInputError)
    }

    @Test
    fun `on setServer should change the server`() {
        assertFalse(sut.loginState.value.isServerInputError)
        assertEquals(ApiConstants.DEFAULT_HOST, sut.loginState.value.server.text)

        sut.loginState.value.onServerValueChange(TextFieldValue("server"))

        assertEquals("server", sut.loginState.value.server.text)
        assertFalse(sut.loginState.value.isServerInputError)
    }
}
