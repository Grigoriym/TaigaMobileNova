package com.grappim.taigamobile.core.api

import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class FakeX509TrustManager(private val serverTrustedThrows: Boolean = true) : X509TrustManager {

    var checkServerTrustedCalled = false

    override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) = Unit

    override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {
        checkServerTrustedCalled = true
        if (serverTrustedThrows) throw CertificateException("untrusted")
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
}
