package com.grappim.taigamobile.core.api.errors

import com.grappim.taigamobile.core.domain.NetworkException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
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
}
