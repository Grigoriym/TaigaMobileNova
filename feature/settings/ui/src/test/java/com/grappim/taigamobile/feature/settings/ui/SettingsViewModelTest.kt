package com.grappim.taigamobile.feature.settings.ui

import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.ThemeSettings
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.getUser
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
    private val taigaSessionStorage = mockk<TaigaSessionStorage>()
    private val serverStorage = mockk<ServerStorage>()
    private val appInfoProvider = mockk<AppInfoProvider>()

    private val userResult = getUser()
    private val appInfo = getRandomString()
    private val server = getRandomString()

    @Before
    fun setup() {
        coEvery { usersRepository.getMe() } returns userResult
        every { appInfoProvider.getAppInfo() } returns appInfo
        every { serverStorage.server } returns server
        every { taigaSessionStorage.themeSettings } returns flowOf(ThemeSettings.default())

        sut = SettingsViewModel(
            usersRepository = usersRepository,
            taigaSessionStorage = taigaSessionStorage,
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
        coEvery { taigaSessionStorage.setThemSetting(newTheme) } just Runs
        every { taigaSessionStorage.themeSettings } returns flowOf(newTheme)
        assertEquals(ThemeSettings.default(), sut.state.value.themeSettings)

        sut.state.value.onThemeChanged(newTheme)

        coVerify { taigaSessionStorage.setThemSetting(newTheme) }
    }
}
