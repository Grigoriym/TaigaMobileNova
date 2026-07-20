package com.grappim.taigamobile.core.domain

import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

actual fun mapPlatformNetworkErrorCode(exception: Exception): Int? = when (exception) {
    is SSLHandshakeException -> NetworkException.ERROR_SSL_CERTIFICATE
    is UnknownHostException -> NetworkException.ERROR_HOST_NOT_FOUND
    else -> null
}
