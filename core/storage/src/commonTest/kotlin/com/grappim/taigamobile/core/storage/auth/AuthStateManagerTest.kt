package com.grappim.taigamobile.core.storage.auth

import app.cash.turbine.test
import com.grappim.taigamobile.testing.storage.FakeAuthStorage
import com.grappim.taigamobile.testing.storage.FakeDatabaseWrapper
import com.grappim.taigamobile.testing.storage.FakeFiltersStorage
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthStateManagerTest {

    private lateinit var filtersStorage: FakeFiltersStorage
    private lateinit var fakeTaigaSessionStorage: FakeTaigaSessionStorage
    private lateinit var fakeAuthStorage: FakeAuthStorage
    private lateinit var fakeDatabaseWrapper: FakeDatabaseWrapper

    @BeforeTest
    fun setup() {
        filtersStorage = FakeFiltersStorage()
        fakeTaigaSessionStorage = FakeTaigaSessionStorage()
        fakeAuthStorage = FakeAuthStorage()
        fakeDatabaseWrapper = FakeDatabaseWrapper()
    }

    private fun createSut(scope: kotlinx.coroutines.CoroutineScope) = AuthStateManager(
        filtersStorage = filtersStorage,
        taigaSessionStorage = fakeTaigaSessionStorage,
        authStorage = fakeAuthStorage,
        databaseWrapper = fakeDatabaseWrapper,
        applicationScope = scope
    )

    @Test
    fun `logoutSuspend resets session clears all storages and emits UserInitiated event`() = runTest {
        val sut = createSut(this)

        sut.logoutEvents.test {
            sut.logoutSuspend()

            assertTrue(filtersStorage.resetFiltersCalled)
            assertTrue(fakeTaigaSessionStorage.clearDataCalled)
            assertTrue(fakeAuthStorage.clearCalled)
            assertTrue(fakeDatabaseWrapper.clearAllTablesCalled)
            assertEquals(LogoutEvent.UserInitiated, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `logout triggers cleanup and emits UserInitiated event via application scope`() = runTest {
        val sut = createSut(this)

        sut.logoutEvents.test {
            sut.logout()

            assertEquals(LogoutEvent.UserInitiated, awaitItem())
            assertTrue(filtersStorage.resetFiltersCalled)
            assertTrue(fakeTaigaSessionStorage.clearDataCalled)
            assertTrue(fakeAuthStorage.clearCalled)
            assertTrue(fakeDatabaseWrapper.clearAllTablesCalled)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
