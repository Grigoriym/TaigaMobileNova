package com.grappim.taigamobile.feature.settings.ui.user

import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.storage.FakeServerStorage
import com.grappim.taigamobile.testing.utils.testException
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

internal class SettingsUserScreenViewModelTest {

    private val usersRepository = FakeUsersRepository()
    private val serverStorage = FakeServerStorage()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: SettingsUserScreenViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = SettingsUserScreenViewModel(
            usersRepository = usersRepository,
            serverStorage = serverStorage
        )
    }

    /**
     * [MainDispatcherRule] uses an unconfined dispatcher, so the `init` block's coroutine has
     * already completed by the time [createViewModel] returns — `isLoading = true` is never
     * observable from the outside and these tests assert the post-load state.
     */
    @Test
    fun `on init - success - user is loaded and isLoading is false`() = runTest {
        val user = getUser()
        usersRepository.getMeResult = user

        createViewModel()

        with(sut.state.value) {
            assertEquals(user, this.user)
            assertFalse(isLoading)
            assertEquals(NativeText.Empty, error)
        }
        assertEquals(1, usersRepository.getMeCallCount)
    }

    @Test
    fun `on init - server url is read from the storage`() = runTest {
        usersRepository.getMeResult = getUser()
        serverStorage.server = "https://taiga.internal.example"

        createViewModel()

        assertEquals("https://taiga.internal.example", sut.state.value.serverUrl)
    }

    @Test
    fun `on init - failure - error is set and user stays null`() = runTest {
        usersRepository.getMeThrows = testException

        createViewModel()

        with(sut.state.value) {
            assertNull(user)
            assertFalse(isLoading)
            assertEquals(NativeText.Simple(testException.message.toString()), error)
        }
        assertEquals(1, usersRepository.getMeCallCount)
    }

    @Test
    fun `on init - failure - server url is still exposed`() = runTest {
        usersRepository.getMeThrows = testException
        serverStorage.server = "https://taiga.internal.example"

        createViewModel()

        assertEquals("https://taiga.internal.example", sut.state.value.serverUrl)
    }
}
