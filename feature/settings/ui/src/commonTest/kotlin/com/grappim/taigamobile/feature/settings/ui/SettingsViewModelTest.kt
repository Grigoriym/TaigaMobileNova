package com.grappim.taigamobile.feature.settings.ui

import com.grappim.taigamobile.core.storage.auth.AuthStateManager
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getProjectSimple
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.storage.FakeAuthStorage
import com.grappim.taigamobile.testing.storage.FakeDatabaseWrapper
import com.grappim.taigamobile.testing.storage.FakeFiltersStorage
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SettingsViewModelTest {

    private val projectsRepository = FakeProjectsRepository()
    private val filtersStorage = FakeFiltersStorage()
    private val taigaSessionStorage = FakeTaigaSessionStorage()
    private val authStorage = FakeAuthStorage()
    private val databaseWrapper = FakeDatabaseWrapper()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: SettingsViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        val authStateManager = AuthStateManager(
            filtersStorage = filtersStorage,
            taigaSessionStorage = taigaSessionStorage,
            authStorage = authStorage,
            databaseWrapper = databaseWrapper,
            applicationScope = CoroutineScope(SupervisorJob())
        )
        sut = SettingsViewModel(
            projectsRepository = projectsRepository,
            authStateManager = authStateManager
        )
    }

    @Test
    fun `on init - project is admin - canSeeAttributes is true`() {
        projectsRepository.getCurrentProjectSimpleResult = getProjectSimple().copy(isAdmin = true)

        createViewModel()

        assertTrue(sut.state.value.canSeeAttributes)
    }

    @Test
    fun `on init - project is not admin - canSeeAttributes is false`() {
        projectsRepository.getCurrentProjectSimpleResult = getProjectSimple().copy(isAdmin = false)

        createViewModel()

        assertFalse(sut.state.value.canSeeAttributes)
    }

    /**
     * Regression for the fdroid-debug crash on real device: `getCurrentProjectSimple()` throwing
     * (e.g. the project row already gone) must not crash the ViewModel — `resultOf` catches it and
     * `canSeeAttributes` is simply left at its default.
     */
    @Test
    fun `on init - project load throws - canSeeAttributes stays false`() {
        projectsRepository.getCurrentProjectSimpleThrows = testException

        createViewModel()

        assertFalse(sut.state.value.canSeeAttributes)
    }

    @Test
    fun `logout - hides confirmation dialog and calls logoutSuspend`() {
        projectsRepository.getCurrentProjectSimpleResult = getProjectSimple()
        createViewModel()
        sut.state.value.setIsLogoutConfirmationVisible(true)
        assertTrue(sut.state.value.isLogoutConfirmationVisible)

        sut.state.value.onLogout()

        assertFalse(sut.state.value.isLogoutConfirmationVisible)
        assertTrue(filtersStorage.resetFiltersCalled)
    }
}
