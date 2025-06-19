package com.grappim.taigamobile.viewmodels

import com.grappim.taigamobile.feature.login.domain.model.AuthType
import com.grappim.taigamobile.login.ui.LoginViewModel
import com.grappim.taigamobile.ui.utils.ErrorResult
import com.grappim.taigamobile.ui.utils.SuccessResult
import com.grappim.taigamobile.viewmodels.utils.accessDeniedException
import io.mockk.coEvery
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

class LoginViewModelTest : BaseViewModelTest() {
    private lateinit var viewModel: LoginViewModel

    @BeforeTest
    fun setup() {
        viewModel = LoginViewModel(mockAppComponent)
    }

    @Test
    fun `test login`(): Unit = runBlocking {
        val password = "password"

        coEvery {
            mockAuthRepository.auth(
                any(),
                any(),
                neq(password),
                any()
            )
        } throws accessDeniedException

        viewModel.login("", AuthType.NORMAL, "", password)
        assertIs<SuccessResult<Unit>>(viewModel.loginResult.value)

        viewModel.login("", AuthType.NORMAL, "", password + "wrong")
        assertIs<ErrorResult<Unit>>(viewModel.loginResult.value)
    }
}
