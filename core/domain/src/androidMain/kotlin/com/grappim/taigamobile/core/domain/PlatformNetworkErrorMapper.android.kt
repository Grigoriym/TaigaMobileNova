package com.grappim.taigamobile.core.domain

import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

actual fun mapPlatformNetworkError(exception: Exception): PlatformNetworkError? = when {
    exception is SSLHandshakeException && exception.cause is UntrustedCertificateException ->
        PlatformNetworkError.UntrustedCertificate(
            (exception.cause as UntrustedCertificateException).pendingCertTrust
        )

    exception is SSLHandshakeException && exception.cause is CertificateHostnameMismatchException ->
        PlatformNetworkError.Code(NetworkException.ERROR_SSL_HOSTNAME_MISMATCH)

    exception is SSLHandshakeException -> PlatformNetworkError.Code(NetworkException.ERROR_SSL_CERTIFICATE)

    exception is SSLPeerUnverifiedException -> PlatformNetworkError.Code(NetworkException.ERROR_SSL_HOSTNAME_MISMATCH)

    exception is UnknownHostException -> PlatformNetworkError.Code(NetworkException.ERROR_HOST_NOT_FOUND)

    else -> null
}
