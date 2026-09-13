package com.grappim.taigamobile.core.api.errors

import com.grappim.kit.domain.CertificateHostnameMismatchException
import com.grappim.kit.domain.PendingCertTrust
import com.grappim.kit.domain.UntrustedCertificateException
import com.grappim.taigamobile.core.domain.NetworkException
import com.grappim.taigamobile.core.domain.UntrustedCertificateNetworkException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkErrorMapperJvmTest {

    private lateinit var sut: NetworkErrorMapper

    @BeforeTest
    fun setup() {
        sut = NetworkErrorMapper()
    }

    @Test
    fun `mapToNetworkException with SSLHandshakeException returns NetworkException with ERROR_SSL_CERTIFICATE`() {
        val e = SSLHandshakeException("certificate not trusted")

        val result = sut.mapToNetworkException(e)

        assertEquals(NetworkException.ERROR_SSL_CERTIFICATE, (result as NetworkException).errorCode)
    }

    @Test
    fun `mapToNetworkException with UnknownHostException returns NetworkException with ERROR_HOST_NOT_FOUND`() {
        val e = UnknownHostException("taiga.example.com")

        val result = sut.mapToNetworkException(e)

        assertEquals(NetworkException.ERROR_HOST_NOT_FOUND, (result as NetworkException).errorCode)
    }

    @Test
    fun `mapToNetworkException unwraps an UntrustedCertificateException into UntrustedCertificateNetworkException`() {
        val pendingCertTrust = PendingCertTrust(
            host = "taiga.example.com",
            subject = "CN=taiga.example.com",
            issuer = "CN=Home Lab CA",
            notBefore = "2026-01-01",
            notAfter = "2027-01-01",
            sha256Fingerprint = "AA:BB:CC"
        )
        // CompositeTrustManager wraps the kit exception in a plain CertificateException so JSSE's
        // handshake code (which only recognizes CertificateException) accepts the throw — the kit
        // type itself is no longer a CertificateException subtype. Mirror that two-level chain here
        // rather than attaching the kit exception directly, or this test would pass without proving
        // the mapper's chain walk actually reaches through the wrapper.
        val kitCause = UntrustedCertificateException(pendingCertTrust, CertificateExceptionStub())
        val cause = CertificateExceptionStub(kitCause)
        val e = SSLHandshakeException("certificate not trusted").apply { initCause(cause) }

        val result = sut.mapToNetworkException(e)

        assertEquals(pendingCertTrust, (result as UntrustedCertificateNetworkException).pendingCertTrust)
    }

    @Test
    fun `mapToNetworkException with a plain SSLHandshakeException still falls back to ERROR_SSL_CERTIFICATE`() {
        val e = SSLHandshakeException("certificate not trusted").apply { initCause(RuntimeException("unrelated")) }

        val result = sut.mapToNetworkException(e)

        assertEquals(NetworkException.ERROR_SSL_CERTIFICATE, (result as NetworkException).errorCode)
    }

    @Test
    fun `mapToNetworkException with SSLPeerUnverifiedException returns ERROR_SSL_HOSTNAME_MISMATCH`() {
        val e = SSLPeerUnverifiedException("Hostname 192.168.0.248 not verified")

        val result = sut.mapToNetworkException(e)

        assertEquals(NetworkException.ERROR_SSL_HOSTNAME_MISMATCH, (result as NetworkException).errorCode)
    }

    @Test
    fun `mapToNetworkException unwraps a CertificateHostnameMismatchException into ERROR_SSL_HOSTNAME_MISMATCH`() {
        val cause = CertificateExceptionStub(
            CertificateHostnameMismatchException("host mismatch", CertificateExceptionStub())
        )
        val e = SSLHandshakeException("certificate not trusted").apply { initCause(cause) }

        val result = sut.mapToNetworkException(e)

        assertEquals(NetworkException.ERROR_SSL_HOSTNAME_MISMATCH, (result as NetworkException).errorCode)
    }
}

private class CertificateExceptionStub(cause: Throwable? = null) :
    java.security.cert.CertificateException("untrusted", cause)
