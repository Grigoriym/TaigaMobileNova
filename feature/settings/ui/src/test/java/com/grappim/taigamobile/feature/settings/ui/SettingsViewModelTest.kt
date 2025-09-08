package com.grappim.taigamobile.feature.settings.ui

import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.core.storage.ThemeSettings
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.getUserDTO
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsViewModelTest {

    @get:Rule
    val coroutineRule = MainDispatcherRule()

    private lateinit var sut: SettingsViewModel

    private val usersRepository = mockk<UsersRepository>()
    private val taigaStorage = mockk<TaigaStorage>()
    private val serverStorage = mockk<ServerStorage>()
    private val appInfoProvider = mockk<AppInfoProvider>()

    private val userResult = Result.success(getUserDTO())
    private val appInfo = getRandomString()
    private val server = getRandomString()

    @Before
    fun setup() {
        coEvery { usersRepository.getMeResult() } returns userResult
        every { appInfoProvider.getAppInfo() } returns appInfo
        every { serverStorage.server } returns server
        every { taigaStorage.themeSettings } returns flowOf(ThemeSettings.default())
        every { taigaStorage.isNewUIUsed } returns flowOf(false)

        sut = SettingsViewModel(
            usersRepository = usersRepository,
            taigaStorage = taigaStorage,
            serverStorage = serverStorage,
            appInfoProvider = appInfoProvider
        )

        assertEquals(appInfo, sut.state.value.appInfo)
        assertEquals(server, sut.state.value.serverUrl)
        verify { appInfoProvider.getAppInfo() }
    }

    @Test
    fun `on switchTheme, should switch theme`() = runTest {
        val newTheme = ThemeSettings.Dark
        coEvery { taigaStorage.setThemSetting(newTheme) } just Runs
        every { taigaStorage.themeSettings } returns flowOf(newTheme)
        assertEquals(ThemeSettings.default(), sut.state.value.themeSettings)

        sut.state.value.onThemeChanged(newTheme)

        coVerify { taigaStorage.setThemSetting(newTheme) }
    }

    @Test
    fun `on isNewUIUsed toggle, should switch isNewUIUsed`() = runTest {
        val expected = true
        coEvery { taigaStorage.setIsUIUsed(expected) } just Runs
        every { taigaStorage.isNewUIUsed } returns flowOf(expected)
        assertEquals(false, sut.state.value.isNewUIUsed)

        sut.state.value.onNewUIToggle()

        coVerify { taigaStorage.setIsUIUsed(expected) }
    }
}
