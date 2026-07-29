package com.grappim.taigamobile.core.api.errors

import com.grappim.taigamobile.core.domain.NetworkException
import com.grappim.taigamobile.core.domain.PlatformIOException
import com.grappim.taigamobile.core.domain.PlatformNetworkError
import com.grappim.taigamobile.core.domain.UntrustedCertificateNetworkException
import com.grappim.taigamobile.core.domain.mapPlatformNetworkError
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.http.HttpStatusCode
import org.koin.core.annotation.Single

@Single
class NetworkErrorMapper {
    fun mapToErrorCode(errorCode: Int): Int = when (errorCode) {
        HttpStatusCode.BadRequest.value -> NetworkException.ERROR_VALIDATION
        HttpStatusCode.Unauthorized.value -> NetworkException.ERROR_UNAUTHORIZED
        HttpStatusCode.Forbidden.value -> NetworkException.ERROR_PERMISSION_DENIED
        HttpStatusCode.NotFound.value -> NetworkException.ERROR_NOT_FOUND
        HttpStatusCode.Conflict.value -> NetworkException.ERROR_CONFLICT
        HttpStatusCode.UnprocessableEntity.value -> NetworkException.ERROR_VALIDATION
        423 -> NetworkException.ERROR_LOCKED
        in 500..599 -> NetworkException.ERROR_INTERNAL_SERVER
        else -> NetworkException.ERROR_HTTP_EXCEPTION
    }

    fun mapToNetworkException(e: Exception): Throwable {
        val platformError = mapPlatformNetworkError(e)
        return when {
            e is NetworkException -> e

            e is SocketTimeoutException -> NetworkException(NetworkException.ERROR_TIMEOUT)

            platformError is PlatformNetworkError.UntrustedCertificate ->
                UntrustedCertificateNetworkException(platformError.pendingCertTrust)

            platformError is PlatformNetworkError.Code -> NetworkException(platformError.errorCode)

            e is PlatformIOException -> NetworkException(NetworkException.ERROR_NETWORK_IO)

            else -> NetworkException(NetworkException.ERROR_UNDEFINED)
        }
    }
}
