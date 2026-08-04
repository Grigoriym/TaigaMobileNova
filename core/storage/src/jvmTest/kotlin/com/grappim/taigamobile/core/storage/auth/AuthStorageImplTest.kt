package com.grappim.taigamobile.core.storage.auth

import com.grappim.taigamobile.core.storage.createTestDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthStorageImplTest {

    private fun createSut() = AuthStorageImpl(createTestDataStore("auth_storage_test"))

    @Test
    fun `getToken returns an empty string when nothing was ever stored`() = runTest {
        val sut = createSut()

        assertEquals("", sut.getToken())
    }

    @Test
    fun `getRefreshToken returns an empty string when nothing was ever stored`() = runTest {
        val sut = createSut()

        assertEquals("", sut.getRefreshToken())
    }

    @Test
    fun `setAuthCredentials stores both tokens`() = runTest {
        val sut = createSut()

        sut.setAuthCredentials(token = "access", refreshToken = "refresh")

        assertEquals("access", sut.getToken())
        assertEquals("refresh", sut.getRefreshToken())
    }

    @Test
    fun `setAuthCredentials overwrites previously stored tokens`() = runTest {
        val sut = createSut()
        sut.setAuthCredentials(token = "old", refreshToken = "oldRefresh")

        sut.setAuthCredentials(token = "new", refreshToken = "newRefresh")

        assertEquals("new", sut.getToken())
        assertEquals("newRefresh", sut.getRefreshToken())
    }

    @Test
    fun `isLoggedIn is false when neither token is stored`() = runTest {
        val sut = createSut()

        assertFalse(sut.isLoggedIn.first())
    }

    @Test
    fun `isLoggedIn is true when both tokens are non-empty`() = runTest {
        val sut = createSut()

        sut.setAuthCredentials(token = "access", refreshToken = "refresh")

        assertTrue(sut.isLoggedIn.first())
    }

    @Test
    fun `isLoggedIn is false when only the access token is non-empty`() = runTest {
        val sut = createSut()

        sut.setAuthCredentials(token = "access", refreshToken = "")

        assertFalse(sut.isLoggedIn.first())
    }

    @Test
    fun `isLoggedIn is false when only the refresh token is non-empty`() = runTest {
        val sut = createSut()

        sut.setAuthCredentials(token = "", refreshToken = "refresh")

        assertFalse(sut.isLoggedIn.first())
    }

    @Test
    fun `clear removes both tokens and logs the user out`() = runTest {
        val sut = createSut()
        sut.setAuthCredentials(token = "access", refreshToken = "refresh")

        sut.clear()

        assertEquals("", sut.getToken())
        assertEquals("", sut.getRefreshToken())
        assertFalse(sut.isLoggedIn.first())
    }

    @Test
    fun `clear on an empty storage is a no-op`() = runTest {
        val sut = createSut()

        sut.clear()

        assertEquals("", sut.getToken())
        assertFalse(sut.isLoggedIn.first())
    }
}
