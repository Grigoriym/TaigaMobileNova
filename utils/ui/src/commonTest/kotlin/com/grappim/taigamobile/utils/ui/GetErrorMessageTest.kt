package com.grappim.taigamobile.utils.ui

import com.grappim.taigamobile.core.domain.NetworkException
import com.grappim.taigamobile.core.domain.PendingCertTrust
import com.grappim.taigamobile.core.domain.TaigaErrorDetails
import com.grappim.taigamobile.core.domain.UntrustedCertificateNetworkException
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.auth_error_refresh_token_not_passed
import com.grappim.taigamobile.strings.generated.resources.connection_failed
import com.grappim.taigamobile.strings.generated.resources.error_blocked
import com.grappim.taigamobile.strings.generated.resources.error_host_not_found
import com.grappim.taigamobile.strings.generated.resources.error_http
import com.grappim.taigamobile.strings.generated.resources.error_internal_server
import com.grappim.taigamobile.strings.generated.resources.error_method_not_allowed
import com.grappim.taigamobile.strings.generated.resources.error_no_internet_connection
import com.grappim.taigamobile.strings.generated.resources.error_not_acceptable
import com.grappim.taigamobile.strings.generated.resources.error_not_found
import com.grappim.taigamobile.strings.generated.resources.error_permission_denied
import com.grappim.taigamobile.strings.generated.resources.error_something_has_gone_wrong
import com.grappim.taigamobile.strings.generated.resources.error_ssl_certificate
import com.grappim.taigamobile.strings.generated.resources.error_ssl_hostname_mismatch
import com.grappim.taigamobile.strings.generated.resources.error_throttled
import com.grappim.taigamobile.strings.generated.resources.error_unsupported_media_type
import com.grappim.taigamobile.strings.generated.resources.error_validation
import com.grappim.taigamobile.strings.generated.resources.request_failed
import com.grappim.taigamobile.strings.generated.resources.timeout_exceeded
import com.grappim.taigamobile.testing.utils.getRandomString
import org.jetbrains.compose.resources.StringResource
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Covers the `errorCode` -> [NativeText] mapping in `getErrorMessage(Throwable)`.
 *
 * The assertions compare [NativeText] instances structurally — they never resolve a
 * [org.jetbrains.compose.resources.StringResource] to an actual string, so nothing here depends on
 * the Compose resource loader or on a locale.
 */
class GetErrorMessageTest {

    @Test
    fun `taiga error message wins over the error code`() {
        val message = getRandomString()
        val exception = NetworkException(
            errorCode = NetworkException.ERROR_NOT_FOUND,
            taigaError = taigaError(message)
        )

        assertEquals(NativeText.Simple(message), getErrorMessage(exception))
    }

    @Test
    fun `error code is used when taigaError is null`() {
        val exception = NetworkException(errorCode = NetworkException.ERROR_NOT_FOUND, taigaError = null)

        assertEquals(NativeText.Resource(RString.error_not_found), getErrorMessage(exception))
    }

    @Test
    fun `ERROR_ON_REFRESH maps to the refresh token string`() {
        assertMapsTo(NetworkException.ERROR_ON_REFRESH, RString.auth_error_refresh_token_not_passed)
    }

    @Test
    fun `ERROR_NO_INTERNET maps to the no internet string`() {
        assertMapsTo(NetworkException.ERROR_NO_INTERNET, RString.error_no_internet_connection)
    }

    @Test
    fun `ERROR_HOST_NOT_FOUND maps to the host not found string`() {
        assertMapsTo(NetworkException.ERROR_HOST_NOT_FOUND, RString.error_host_not_found)
    }

    @Test
    fun `ERROR_TIMEOUT maps to the timeout string`() {
        assertMapsTo(NetworkException.ERROR_TIMEOUT, RString.timeout_exceeded)
    }

    @Test
    fun `ERROR_NETWORK_IO maps to the connection failed string`() {
        assertMapsTo(NetworkException.ERROR_NETWORK_IO, RString.connection_failed)
    }

    @Test
    fun `ERROR_SSL_CERTIFICATE maps to the ssl certificate string`() {
        assertMapsTo(NetworkException.ERROR_SSL_CERTIFICATE, RString.error_ssl_certificate)
    }

    @Test
    fun `ERROR_SSL_HOSTNAME_MISMATCH maps to the hostname mismatch string`() {
        assertMapsTo(NetworkException.ERROR_SSL_HOSTNAME_MISMATCH, RString.error_ssl_hostname_mismatch)
    }

    @Test
    fun `ERROR_UNDEFINED maps to the request failed string`() {
        assertMapsTo(NetworkException.ERROR_UNDEFINED, RString.request_failed)
    }

    @Test
    fun `ERROR_UNAUTHORIZED maps to the refresh token string`() {
        assertMapsTo(NetworkException.ERROR_UNAUTHORIZED, RString.auth_error_refresh_token_not_passed)
    }

    @Test
    fun `ERROR_PERMISSION_DENIED maps to the permission denied string`() {
        assertMapsTo(NetworkException.ERROR_PERMISSION_DENIED, RString.error_permission_denied)
    }

    @Test
    fun `ERROR_NOT_FOUND maps to the not found string`() {
        assertMapsTo(NetworkException.ERROR_NOT_FOUND, RString.error_not_found)
    }

    @Test
    fun `ERROR_BLOCKED maps to the blocked string`() {
        assertMapsTo(NetworkException.ERROR_BLOCKED, RString.error_blocked)
    }

    @Test
    fun `ERROR_VALIDATION maps to the validation string`() {
        assertMapsTo(NetworkException.ERROR_VALIDATION, RString.error_validation)
    }

    @Test
    fun `ERROR_METHOD_NOT_ALLOWED maps to the method not allowed string`() {
        assertMapsTo(NetworkException.ERROR_METHOD_NOT_ALLOWED, RString.error_method_not_allowed)
    }

    @Test
    fun `ERROR_NOT_ACCEPTABLE maps to the not acceptable string`() {
        assertMapsTo(NetworkException.ERROR_NOT_ACCEPTABLE, RString.error_not_acceptable)
    }

    @Test
    fun `ERROR_UNSUPPORTED_MEDIA_TYPE maps to the unsupported media type string`() {
        assertMapsTo(NetworkException.ERROR_UNSUPPORTED_MEDIA_TYPE, RString.error_unsupported_media_type)
    }

    @Test
    fun `ERROR_THROTTLED maps to the throttled string`() {
        assertMapsTo(NetworkException.ERROR_THROTTLED, RString.error_throttled)
    }

    @Test
    fun `ERROR_INTERNAL_SERVER maps to the internal server string`() {
        assertMapsTo(NetworkException.ERROR_INTERNAL_SERVER, RString.error_internal_server)
    }

    @Test
    fun `ERROR_HTTP_EXCEPTION maps to the http string`() {
        assertMapsTo(NetworkException.ERROR_HTTP_EXCEPTION, RString.error_http)
    }

    /**
     * `ERROR_API`, `ERROR_CONFLICT` and `ERROR_LOCKED` are declared on [NetworkException] but have no
     * `when` branch of their own, so they take the `else`. So does any code the enum does not know.
     */
    @Test
    fun `error codes without a branch fall back to the generic string`() {
        assertMapsTo(NetworkException.ERROR_API, RString.error_something_has_gone_wrong)
        assertMapsTo(NetworkException.ERROR_CONFLICT, RString.error_something_has_gone_wrong)
        assertMapsTo(NetworkException.ERROR_LOCKED, RString.error_something_has_gone_wrong)
        assertMapsTo(errorCode = 4242, expected = RString.error_something_has_gone_wrong)
    }

    @Test
    fun `UntrustedCertificateNetworkException maps to the ssl certificate string`() {
        val exception = UntrustedCertificateNetworkException(
            PendingCertTrust(
                host = getRandomString(),
                subject = getRandomString(),
                issuer = getRandomString(),
                notBefore = getRandomString(),
                notAfter = getRandomString(),
                sha256Fingerprint = getRandomString()
            )
        )

        assertEquals(NativeText.Resource(RString.error_ssl_certificate), getErrorMessage(exception))
    }

    @Test
    fun `any other throwable falls back to its message`() {
        val message = getRandomString()

        assertEquals(NativeText.Simple(message), getErrorMessage(IllegalStateException(message)))
    }

    @Test
    fun `a throwable without a message falls back to the literal null`() {
        assertEquals(NativeText.Simple("null"), getErrorMessage(IllegalStateException()))
    }

    private fun assertMapsTo(errorCode: Int, expected: StringResource) {
        val exception = NetworkException(errorCode = errorCode)

        assertEquals(NativeText.Resource(expected), getErrorMessage(exception))
    }

    private fun taigaError(message: String) = TaigaErrorDetails(
        message = message,
        type = getRandomString(),
        statusCode = 404
    )
}
