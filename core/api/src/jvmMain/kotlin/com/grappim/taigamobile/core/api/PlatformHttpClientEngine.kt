package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.storage.cert.TrustedCertStorage
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

actual fun createPlatformHttpClientEngine(trustedCertStorage: TrustedCertStorage): HttpClientEngine {
    val trustManager = CompositeTrustManager(defaultSystemTrustManager(), trustedCertStorage)
    val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, arrayOf<TrustManager>(trustManager), null)
    }

    return OkHttp.create {
        config {
            sslSocketFactory(sslContext.socketFactory, trustManager)
        }
    }
}

private fun defaultSystemTrustManager(): X509TrustManager {
    val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    factory.init(null as KeyStore?)
    return factory.trustManagers.filterIsInstance<X509TrustManager>().first()
}
