package com.grappim.taigamobile.feature.settings.ui.trustedcerts

import com.grappim.taigamobile.core.domain.PendingCertTrust
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.storage.FakeTrustedCertStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class TrustedCertificatesViewModelTest {

    private val trustedCertStorage = FakeTrustedCertStorage()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: TrustedCertificatesViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = TrustedCertificatesViewModel(trustedCertStorage = trustedCertStorage)
    }

    private fun makeCertTrust(host: String, sha256Fingerprint: String) = PendingCertTrust(
        host = host,
        subject = "CN=$host",
        issuer = "CN=Test CA",
        notBefore = "2026-01-01",
        notAfter = "2027-01-01",
        sha256Fingerprint = sha256Fingerprint
    )

    @Test
    fun `on init - no trusted certs - entries is empty`() {
        createViewModel()

        assertTrue(sut.state.value.entries.isEmpty())
    }

    @Test
    fun `on init - trusted certs already stored - entries reflects them`() = runTest {
        val entry = makeCertTrust("taiga.example.com", "AA:BB:CC")
        trustedCertStorage.trust(entry)

        createViewModel()

        assertEquals(listOf(entry), sut.state.value.entries)
    }

    @Test
    fun `onRevokeClick - sets entryToRevoke and shows dialog`() = runTest {
        trustedCertStorage.trust(makeCertTrust("taiga.example.com", "AA:BB:CC"))
        createViewModel()

        val entry = sut.state.value.entries.first()
        sut.state.value.onRevokeClick(entry)

        with(sut.state.value) {
            assertEquals(entry, entryToRevoke)
            assertTrue(isRevokeDialogVisible)
        }
    }

    @Test
    fun `closeRevokeDialog - clears entryToRevoke and hides dialog without revoking`() = runTest {
        trustedCertStorage.trust(makeCertTrust("taiga.example.com", "AA:BB:CC"))
        createViewModel()

        val entry = sut.state.value.entries.first()
        sut.state.value.onRevokeClick(entry)
        sut.state.value.closeRevokeDialog()

        with(sut.state.value) {
            assertNull(entryToRevoke)
            assertFalse(isRevokeDialogVisible)
        }
        assertNull(trustedCertStorage.untrustCalledWith)
        assertEquals(1, sut.state.value.entries.size)
    }

    @Test
    fun `revokeEntry - removes the entry from storage and state, hides dialog`() = runTest {
        trustedCertStorage.trust(makeCertTrust("taiga.example.com", "AA:BB:CC"))
        trustedCertStorage.trust(makeCertTrust("other.example.com", "DD:EE:FF"))
        createViewModel()

        val entryToRevoke = sut.state.value.entries.first { it.host == "taiga.example.com" }
        sut.state.value.onRevokeClick(entryToRevoke)
        sut.state.value.revokeEntry()

        assertEquals("taiga.example.com" to "AA:BB:CC", trustedCertStorage.untrustCalledWith)
        with(sut.state.value) {
            assertNull(this.entryToRevoke)
            assertFalse(isRevokeDialogVisible)
            assertEquals(listOf("other.example.com"), entries.map { it.host })
        }
    }
}
