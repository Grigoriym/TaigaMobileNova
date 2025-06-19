package com.grappim.taigamobile.viewmodels

import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.storage.ThemeSetting
import com.grappim.taigamobile.settings.SettingsViewModel
import com.grappim.taigamobile.ui.utils.ErrorResult
import com.grappim.taigamobile.ui.utils.SuccessResult
import com.grappim.taigamobile.viewmodels.utils.assertResultEquals
import com.grappim.taigamobile.viewmodels.utils.notFoundException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs


class SettingsViewModelTest : BaseViewModelTest() {
    private lateinit var viewModel: SettingsViewModel

    @BeforeTest
    fun setup() {
        viewModel = SettingsViewModel(mockAppComponent)
    }

    @BeforeTest
    fun settingOfUsers() {
        coEvery { mockUsersRepository.getMe() } returns mockUser
    }

    companion object {
        val mockUser = mockk<User>(relaxed = true)
    }

    @Test
    fun `test on open`(): Unit = runBlocking {
        viewModel.onOpen()
        assertResultEquals(SuccessResult(mockUser), viewModel.user.value)

        coEvery { mockUsersRepository.getMe() } throws notFoundException
        viewModel.onOpen()
        assertIs<ErrorResult<User>>(viewModel.user.value)
    }

    @Test
    fun `test logout`(): Unit = runBlocking {
        viewModel.logout()
        coVerify { mockSession.reset() }
    }

    @Test
    fun `test switch theme`(): Unit = runBlocking {
        val themeSetting = ThemeSetting.Light
        viewModel.switchTheme(themeSetting)
        coVerify { mockSettings.changeThemeSetting(eq(themeSetting)) }
    }
}
