package com.grappim.taigamobile.core.storage.customheaders

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.grappim.kit.storage.NoopSecretCipher
import com.grappim.kit.storage.SecretCipher
import com.grappim.kit.testing.FakeSecretCipher
import com.grappim.taigamobile.core.storage.createTestDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Asserts against the JVM DataStore actual; the class itself is commonMain. */
class CustomHeadersStorageImplTest {

    private val dataStore = createTestDataStore("custom_headers_test")
    private val headers = listOf(CustomHeader("CF-Access-Client-Id", "id"), CustomHeader("X-Token", "secret"))

    private fun createSut(secretCipher: SecretCipher = NoopSecretCipher()) =
        CustomHeadersStorageImpl(dataStore, secretCipher, Json)

    @Test
    fun `nothing stored reads as an empty list`() = runTest {
        val sut = createSut()

        assertEquals(emptyList(), sut.headers.first())
        assertEquals(emptyList(), sut.currentHeaders)
    }

    @Test
    fun `setHeaders round-trips through both the flow and the cache`() = runTest {
        val sut = createSut()

        sut.setHeaders(headers)

        assertEquals(headers, sut.headers.first())
        assertEquals(headers, sut.currentHeaders)
        assertEquals(headers, createSut().currentHeaders)
    }

    @Test
    fun `the stored blob goes through the cipher`() = runTest {
        val sut = createSut(FakeSecretCipher())

        sut.setHeaders(headers)

        val stored = dataStore.data.first()[stringPreferencesKey("custom_headers")].orEmpty()
        assertTrue(stored.startsWith("ENC:"))
        assertEquals(headers, createSut(FakeSecretCipher()).headers.first())
    }

    @Test
    fun `an empty list removes the key`() = runTest {
        val sut = createSut()
        sut.setHeaders(headers)

        sut.setHeaders(emptyList())

        assertTrue(dataStore.data.first().asMap().isEmpty())
        assertEquals(emptyList(), sut.currentHeaders)
    }

    @Test
    fun `a blob that does not decode reads as no headers`() = runTest {
        dataStore.edit { it[stringPreferencesKey("custom_headers")] = "not json" }

        assertEquals(emptyList(), createSut().headers.first())
    }

    @Test
    fun `a blob that does not decrypt reads as no headers`() = runTest {
        val undecryptable = FakeSecretCipher().apply { decryptResult = { null } }
        createSut().setHeaders(headers)

        assertEquals(emptyList(), createSut(undecryptable).headers.first())
    }
}
