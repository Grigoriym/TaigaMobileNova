package com.grappim.taigamobile.core.domain

import java.security.cert.CertificateException

/**
 * Thrown by the composite trust manager when the presented certificate's CN/SAN entries don't
 * cover the host being connected to. Distinct from [UntrustedCertificateException] so
 * [mapPlatformNetworkError] can route it to a clear "wrong host" message instead of offering a
 * trust-on-first-use dialog that could never result in a working connection.
 */
class CertificateHostnameMismatchException(message: String, cause: Throwable) : CertificateException(message, cause)
