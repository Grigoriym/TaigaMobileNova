package com.grappim.taigamobile.core.domain

import com.grappim.kit.domain.CertificateHostnameMismatchException
import com.grappim.kit.domain.findPendingCertTrust
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

actual fun mapPlatformNetworkError(exception: Exception): PlatformNetworkError? {
    val pendingCertTrust = exception.findPendingCertTrust()
    return when {
        exception is SSLHandshakeException && pendingCertTrust != null ->
            PlatformNetworkError.UntrustedCertificate(pendingCertTrust)

        exception is SSLHandshakeException && exception.hasCause<CertificateHostnameMismatchException>() ->
            PlatformNetworkError.Code(NetworkException.ERROR_SSL_HOSTNAME_MISMATCH)

        exception is SSLHandshakeException -> PlatformNetworkError.Code(NetworkException.ERROR_SSL_CERTIFICATE)

        exception is SSLPeerUnverifiedException ->
            PlatformNetworkError.Code(NetworkException.ERROR_SSL_HOSTNAME_MISMATCH)

        exception is UnknownHostException -> PlatformNetworkError.Code(NetworkException.ERROR_HOST_NOT_FOUND)

        else -> null
    }
}

private inline fun <reified T : Throwable> Throwable.hasCause(): Boolean =
    generateSequence(this) { it.cause }.any { it is T }
