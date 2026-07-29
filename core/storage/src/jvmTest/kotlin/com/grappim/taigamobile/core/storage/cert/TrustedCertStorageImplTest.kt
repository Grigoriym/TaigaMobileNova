package com.grappim.taigamobile.core.storage.cert

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.grappim.taigamobile.core.domain.PendingCertTrust
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TrustedCertStorageImplTest {

    private fun createSut(): TrustedCertStorageImpl {
        val file = File.createTempFile("trusted_cert_storage_test", ".preferences_pb")
        file.deleteOnExit()
        val dataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { file.absolutePath.toPath() }
        )
        return TrustedCertStorageImpl(dataStore, Json { ignoreUnknownKeys = true })
    }

    private fun makeCertTrust(
        host: String,
        sha256Fingerprint: String,
        subject: String = "CN=$host",
        issuer: String = "CN=Test CA"
    ) = PendingCertTrust(
        host = host,
        subject = subject,
        issuer = issuer,
        notBefore = "2026-01-01",
        notAfter = "2027-01-01",
        sha256Fingerprint = sha256Fingerprint
    )

    @Test
    fun `isTrusted returns false for a host and fingerprint that were never trusted`() = runTest {
        val sut = createSut()

        assertFalse(sut.isTrusted("taiga.example.com", "AA:BB:CC"))
    }

    @Test
    fun `trust then isTrusted returns true for the same host and fingerprint`() = runTest {
        val sut = createSut()

        sut.trust(makeCertTrust("taiga.example.com", "AA:BB:CC"))

        assertTrue(sut.isTrusted("taiga.example.com", "AA:BB:CC"))
    }

    @Test
    fun `trust for one host does not grant trust for a different host with the same fingerprint`() = runTest {
        val sut = createSut()

        sut.trust(makeCertTrust("taiga.example.com", "AA:BB:CC"))

        assertFalse(sut.isTrusted("other.example.com", "AA:BB:CC"))
    }

    @Test
    fun `trusting multiple host-fingerprint pairs keeps them independently trusted`() = runTest {
        val sut = createSut()

        sut.trust(makeCertTrust("taiga.example.com", "AA:BB:CC"))
        sut.trust(makeCertTrust("other.example.com", "DD:EE:FF"))

        assertTrue(sut.isTrusted("taiga.example.com", "AA:BB:CC"))
        assertTrue(sut.isTrusted("other.example.com", "DD:EE:FF"))
        assertFalse(sut.isTrusted("taiga.example.com", "DD:EE:FF"))
    }

    @Test
    fun `trust for the same host-fingerprint pair again replaces the stored entry, not duplicates it`() = runTest {
        val sut = createSut()

        sut.trust(makeCertTrust("taiga.example.com", "AA:BB:CC", subject = "CN=old"))
        sut.trust(makeCertTrust("taiga.example.com", "AA:BB:CC", subject = "CN=new"))

        val entries = sut.getAllFlow().first()
        assertEquals(1, entries.size)
        assertEquals("CN=new", entries.single().subject)
    }

    @Test
    fun `getAllFlow is empty when nothing has been trusted`() = runTest {
        val sut = createSut()

        assertEquals(emptyList(), sut.getAllFlow().first())
    }

    @Test
    fun `getAllFlow reflects every trusted entry with its full cert info`() = runTest {
        val sut = createSut()

        val first = makeCertTrust("taiga.example.com", "AA:BB:CC")
        val second = makeCertTrust("other.example.com", "DD:EE:FF")
        sut.trust(first)
        sut.trust(second)

        val entries = sut.getAllFlow().first()
        assertEquals(setOf(first, second), entries.toSet())
    }

    @Test
    fun `untrust removes only the matching host-fingerprint pair`() = runTest {
        val sut = createSut()
        sut.trust(makeCertTrust("taiga.example.com", "AA:BB:CC"))
        sut.trust(makeCertTrust("other.example.com", "DD:EE:FF"))

        sut.untrust("taiga.example.com", "AA:BB:CC")

        assertFalse(sut.isTrusted("taiga.example.com", "AA:BB:CC"))
        assertTrue(sut.isTrusted("other.example.com", "DD:EE:FF"))
    }

    @Test
    fun `untrust for an unknown pair is a no-op`() = runTest {
        val sut = createSut()
        sut.trust(makeCertTrust("taiga.example.com", "AA:BB:CC"))

        sut.untrust("never.example.com", "00:00:00")

        assertTrue(sut.isTrusted("taiga.example.com", "AA:BB:CC"))
    }
}
