package com.grappim.taigamobile.main

import app.cash.turbine.test
import com.grappim.taigamobile.DrawerItemsBuilder
import com.grappim.taigamobile.core.storage.auth.AuthStateManager
import com.grappim.taigamobile.feature.dashboard.ui.DashboardNavDestination
import com.grappim.taigamobile.feature.login.ui.LoginNavDestination
import com.grappim.taigamobile.feature.projectselector.ui.ProjectSelectorNavDestination
import com.grappim.taigamobile.testing.FakeNetworkMonitor
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.storage.FakeAuthStorage
import com.grappim.taigamobile.testing.storage.FakeDatabaseWrapper
import com.grappim.taigamobile.testing.storage.FakeSessionResetter
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class MainViewModelTest {

    private val authStorage = FakeAuthStorage()
    private val taigaSessionStorage = FakeTaigaSessionStorage()
    private val sessionResetter = FakeSessionResetter()
    private val databaseWrapper = FakeDatabaseWrapper()
    private val projectsRepository = FakeProjectsRepository()
    private val networkMonitor = FakeNetworkMonitor()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: MainViewModel

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
            sessionResetter = sessionResetter,
            taigaSessionStorage = taigaSessionStorage,
            authStorage = authStorage,
            databaseWrapper = databaseWrapper,
            applicationScope = CoroutineScope(SupervisorJob())
        )
        sut = MainViewModel(
            taigaSessionStorage = taigaSessionStorage,
            authStorage = authStorage,
            authStateManager = authStateManager,
            projectsRepository = projectsRepository,
            drawerItemsBuilder = DrawerItemsBuilder(),
            networkMonitor = networkMonitor
        )
    }

    // --- initialNavState ---

    @Test
    fun `initialNavState - not logged in - startDestination is Login`() = runTest {
        createViewModel()

        sut.initialNavState.test {
            skipItems(1) // initial isReady=false
            val state = awaitItem()
            assertTrue(state.isReady)
            assertFalse(state.isProjectSelected)
            assertEquals(LoginNavDestination, state.startDestination)
            cancel()
        }
    }

    @Test
    fun `initialNavState - logged in no project - startDestination is ProjectSelector`() = runTest {
        authStorage.setLoggedIn(true)
        createViewModel()

        sut.initialNavState.test {
            skipItems(1)
            val state = awaitItem()
            assertTrue(state.isReady)
            assertFalse(state.isProjectSelected)
            assertTrue(state.startDestination is ProjectSelectorNavDestination)
            cancel()
        }
    }

    @Test
    fun `initialNavState - logged in with project - startDestination is Dashboard`() = runTest {
        authStorage.setLoggedIn(true)
        taigaSessionStorage.setCurrentProjectId(42L)
        createViewModel()

        sut.initialNavState.test {
            skipItems(1)
            val state = awaitItem()
            assertTrue(state.isReady)
            assertTrue(state.isProjectSelected)
            assertEquals(DashboardNavDestination, state.startDestination)
            cancel()
        }
    }

    // --- logout ---

    @Test
    fun `logout - hides confirmation dialog and calls logoutSuspend`() = runTest {
        createViewModel()
        sut.state.value.setIsLogoutConfirmationVisible(true)
        assertTrue(sut.state.value.isLogoutConfirmationVisible)

        sut.state.value.onLogout()

        assertFalse(sut.state.value.isLogoutConfirmationVisible)
        assertTrue(sessionResetter.resetCalled)
    }

    // --- isOffline ---

    @Test
    fun `isOffline - reflects networkMonitor isOnline`() = runTest {
        createViewModel()

        sut.isOffline.test {
            assertFalse(awaitItem()) // online → not offline
            networkMonitor.setOnline(false)
            assertTrue(awaitItem())
            cancel()
        }
    }
}
