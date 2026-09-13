package com.grappim.taigamobile.utils.ui

import com.grappim.kit.uikit.NativeText
import com.grappim.taigamobile.core.domain.NetworkException
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

fun getErrorMessage(exception: Throwable): NativeText = if (exception is NetworkException) {
    exception.taigaError?.message?.let { NativeText.Simple(it) } ?: when (exception.errorCode) {
        NetworkException.ERROR_ON_REFRESH -> NativeText.Resource(
            RString.auth_error_refresh_token_not_passed
        )

        NetworkException.ERROR_NO_INTERNET -> NativeText.Resource(
            RString.error_no_internet_connection
        )

        NetworkException.ERROR_HOST_NOT_FOUND -> NativeText.Resource(
            RString.error_host_not_found
        )

        NetworkException.ERROR_TIMEOUT -> NativeText.Resource(RString.timeout_exceeded)

        NetworkException.ERROR_NETWORK_IO -> NativeText.Resource(RString.connection_failed)

        NetworkException.ERROR_SSL_CERTIFICATE -> NativeText.Resource(
            RString.error_ssl_certificate
        )

        NetworkException.ERROR_SSL_HOSTNAME_MISMATCH -> NativeText.Resource(
            RString.error_ssl_hostname_mismatch
        )

        NetworkException.ERROR_UNDEFINED -> NativeText.Resource(RString.request_failed)

        NetworkException.ERROR_UNAUTHORIZED -> NativeText.Resource(
            RString.auth_error_refresh_token_not_passed
        )

        NetworkException.ERROR_PERMISSION_DENIED -> NativeText.Resource(
            RString.error_permission_denied
        )

        NetworkException.ERROR_NOT_FOUND -> NativeText.Resource(
            RString.error_not_found
        )

        NetworkException.ERROR_BLOCKED -> NativeText.Resource(
            RString.error_blocked
        )

        NetworkException.ERROR_VALIDATION -> NativeText.Resource(
            RString.error_validation
        )

        NetworkException.ERROR_METHOD_NOT_ALLOWED -> NativeText.Resource(
            RString.error_method_not_allowed
        )

        NetworkException.ERROR_NOT_ACCEPTABLE -> NativeText.Resource(
            RString.error_not_acceptable
        )

        NetworkException.ERROR_UNSUPPORTED_MEDIA_TYPE -> NativeText.Resource(
            RString.error_unsupported_media_type
        )

        NetworkException.ERROR_THROTTLED -> NativeText.Resource(
            RString.error_throttled
        )

        NetworkException.ERROR_INTERNAL_SERVER -> NativeText.Resource(
            RString.error_internal_server
        )

        NetworkException.ERROR_HTTP_EXCEPTION -> NativeText.Resource(
            RString.error_http
        )

        else -> NativeText.Resource(RString.error_something_has_gone_wrong)
    }
} else if (exception is UntrustedCertificateNetworkException) {
    // Callers that specifically handle UntrustedCertificateNetworkException (e.g. the login flow)
    // check for it before calling getErrorMessage and show a trust-this-certificate prompt instead.
    // This is the fallback for everyone else.
    NativeText.Resource(RString.error_ssl_certificate)
} else {
    NativeText.Simple(exception.message.toString())
}
