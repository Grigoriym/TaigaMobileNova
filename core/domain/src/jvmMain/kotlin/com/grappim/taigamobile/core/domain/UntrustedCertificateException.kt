package com.grappim.taigamobile.core.domain

import java.security.cert.CertificateException

/**
 * Thrown by the composite trust manager instead of the plain [CertificateException] a default
 * trust manager would throw, so the presented certificate's details survive the
 * SSLHandshakeException wrapping all the way up to [mapPlatformNetworkError] and can be shown to
 * the user instead of a generic failure.
 */
class UntrustedCertificateException(val pendingCertTrust: PendingCertTrust, cause: Throwable) :
    CertificateException(cause)
