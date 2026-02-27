package com.grappim.taigamobile.core.api.errors

import com.grappim.taigamobile.core.domain.NetworkException
import io.ktor.client.network.sockets.SocketTimeoutException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class NetworkErrorMapperTest {

    private lateinit var sut: NetworkErrorMapper

    @BeforeTest
    fun setup() {
        sut = NetworkErrorMapper()
    }

    // region mapToErrorCode

    @Test
    fun `mapToErrorCode 400 returns ERROR_VALIDATION`() {
        assertEquals(NetworkException.ERROR_VALIDATION, sut.mapToErrorCode(400))
    }

    @Test
    fun `mapToErrorCode 401 returns ERROR_UNAUTHORIZED`() {
        assertEquals(NetworkException.ERROR_UNAUTHORIZED, sut.mapToErrorCode(401))
    }

    @Test
    fun `mapToErrorCode 403 returns ERROR_PERMISSION_DENIED`() {
        assertEquals(NetworkException.ERROR_PERMISSION_DENIED, sut.mapToErrorCode(403))
    }

    @Test
    fun `mapToErrorCode 404 returns ERROR_NOT_FOUND`() {
        assertEquals(NetworkException.ERROR_NOT_FOUND, sut.mapToErrorCode(404))
    }

    @Test
    fun `mapToErrorCode 409 returns ERROR_CONFLICT`() {
        assertEquals(NetworkException.ERROR_CONFLICT, sut.mapToErrorCode(409))
    }

    @Test
    fun `mapToErrorCode 422 returns ERROR_VALIDATION`() {
        assertEquals(NetworkException.ERROR_VALIDATION, sut.mapToErrorCode(422))
    }

    @Test
    fun `mapToErrorCode 423 returns ERROR_LOCKED`() {
        assertEquals(NetworkException.ERROR_LOCKED, sut.mapToErrorCode(423))
    }

    @Test
    fun `mapToErrorCode 500 returns ERROR_INTERNAL_SERVER`() {
        assertEquals(NetworkException.ERROR_INTERNAL_SERVER, sut.mapToErrorCode(500))
    }

    @Test
    fun `mapToErrorCode 503 returns ERROR_INTERNAL_SERVER`() {
        assertEquals(NetworkException.ERROR_INTERNAL_SERVER, sut.mapToErrorCode(503))
    }

    @Test
    fun `mapToErrorCode 599 returns ERROR_INTERNAL_SERVER`() {
        assertEquals(NetworkException.ERROR_INTERNAL_SERVER, sut.mapToErrorCode(599))
    }

    @Test
    fun `mapToErrorCode unknown code returns ERROR_HTTP_EXCEPTION`() {
        assertEquals(NetworkException.ERROR_HTTP_EXCEPTION, sut.mapToErrorCode(418))
    }

    @Test
    fun `mapToErrorCode 200 returns ERROR_HTTP_EXCEPTION`() {
        assertEquals(NetworkException.ERROR_HTTP_EXCEPTION, sut.mapToErrorCode(200))
    }

    // endregion

    // region mapToNetworkException

    @Test
    fun `mapToNetworkException with NetworkException returns the same instance`() {
        val original = NetworkException(NetworkException.ERROR_NOT_FOUND)

        val result = sut.mapToNetworkException(original)

        assertSame(original, result)
    }

    @Test
    fun `mapToNetworkException with SocketTimeoutException returns NetworkException with ERROR_TIMEOUT`() {
        val e = SocketTimeoutException("timeout", null)

        val result = sut.mapToNetworkException(e)

        assertEquals(NetworkException.ERROR_TIMEOUT, (result as NetworkException).errorCode)
    }

    @Test
    fun `mapToNetworkException with unknown Exception returns NetworkException with ERROR_UNDEFINED`() {
        val e = RuntimeException("unexpected")

        val result = sut.mapToNetworkException(e)

        assertEquals(NetworkException.ERROR_UNDEFINED, (result as NetworkException).errorCode)
    }

    // endregion
}
