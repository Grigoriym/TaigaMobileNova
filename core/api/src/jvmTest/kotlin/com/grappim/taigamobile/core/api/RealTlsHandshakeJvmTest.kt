package com.grappim.taigamobile.core.api

import com.grappim.kit.testing.FakeTrustedCertStorage
import com.grappim.taigamobile.core.api.errors.NetworkErrorMapper
import com.grappim.taigamobile.core.domain.UntrustedCertificateNetworkException
import com.sun.net.httpserver.HttpsConfigurator
import com.sun.net.httpserver.HttpsServer
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.test.runTest
import java.io.File
import java.net.InetSocketAddress
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Drives `grappim-kit-trustmanager`'s `CompositeTrustManager` through a real JSSE handshake — a
 * real self-signed HTTPS server and the actual [createPlatformHttpClientEngine] wiring — rather
 * than a hand-built exception chain. The kit's own `CompositeTrustManagerTest` and
 * [com.grappim.taigamobile.core.api.errors.NetworkErrorMapperJvmTest] prove the mapping logic in
 * isolation; this proves the JDK's TLS stack still recognizes what `CompositeTrustManager` throws
 * (a plain [java.security.cert.CertificateException] wrapping the grappim-kit-domain exception,
 * which is no longer a [java.security.cert.CertificateException] subtype itself) and produces the
 * same [javax.net.ssl.SSLHandshakeException] chain in practice.
 */
class RealTlsHandshakeJvmTest {

    private lateinit var server: HttpsServer
    private lateinit var keystoreFile: File
    private var port = 0

    @BeforeTest
    fun setup() {
        keystoreFile = File.createTempFile("cert-trust-test", ".p12")
        keystoreFile.delete()
        keystoreFile.deleteOnExit()

        val keytool = File(System.getProperty("java.home"), "bin/keytool").absolutePath
        val process = ProcessBuilder(
            keytool,
            "-genkeypair",
            "-alias",
            "test",
            "-keyalg",
            "RSA",
            "-keysize",
            "2048",
            "-validity",
            "1",
            "-storetype",
            "PKCS12",
            "-keystore",
            keystoreFile.absolutePath,
            "-storepass",
            KEYSTORE_PASSWORD,
            "-keypass",
            KEYSTORE_PASSWORD,
            "-dname",
            "CN=127.0.0.1",
            "-ext",
            "SAN=IP:127.0.0.1"
        ).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText()
        check(process.waitFor() == 0) { "keytool failed: $output" }

        val keyStore = KeyStore.getInstance("PKCS12").apply {
            keystoreFile.inputStream().use { load(it, KEYSTORE_PASSWORD.toCharArray()) }
        }
        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
            init(keyStore, KEYSTORE_PASSWORD.toCharArray())
        }
        val serverSslContext = SSLContext.getInstance("TLS").apply {
            init(keyManagerFactory.keyManagers, null, null)
        }

        server = HttpsServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            httpsConfigurator = HttpsConfigurator(serverSslContext)
            createContext("/") { exchange ->
                val body = "ok".toByteArray()
                exchange.sendResponseHeaders(200, body.size.toLong())
                exchange.responseBody.use { it.write(body) }
            }
            start()
        }
        port = server.address.port
    }

    @AfterTest
    fun tearDown() {
        server.stop(0)
        keystoreFile.delete()
    }

    @Test
    fun `untrusted self-signed cert fails with the real cert details, then succeeds once pinned`() = runTest {
        val trustedCertStorage = FakeTrustedCertStorage()
        val client = HttpClient(createPlatformHttpClientEngine(trustedCertStorage))

        val firstFailure = runCatching { client.get("https://127.0.0.1:$port/") }.exceptionOrNull()
        checkNotNull(firstFailure) { "expected the untrusted self-signed cert to fail the handshake" }

        val mapped = NetworkErrorMapper().mapToNetworkException(firstFailure as Exception)
        val untrusted = mapped as UntrustedCertificateNetworkException
        assertEquals("127.0.0.1", untrusted.pendingCertTrust.host)
        assertTrue(untrusted.pendingCertTrust.sha256Fingerprint.isNotBlank())

        trustedCertStorage.trust(untrusted.pendingCertTrust)

        val response = client.get("https://127.0.0.1:$port/")
        assertEquals(200, response.status.value)

        client.close()
    }

    private companion object {
        const val KEYSTORE_PASSWORD = "changeit"
    }
}
