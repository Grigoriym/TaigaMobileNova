package com.grappim.taigamobile.core.api.errors

import com.grappim.taigamobile.core.domain.NetworkException
import com.grappim.taigamobile.core.domain.PlatformIOException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.http.HttpStatusCode
import org.koin.core.annotation.Single

@Single
class NetworkErrorMapper {
    fun mapToErrorCode(errorCode: Int): Int = when (errorCode) {
        HttpStatusCode.Companion.BadRequest.value -> NetworkException.Companion.ERROR_VALIDATION
        HttpStatusCode.Companion.Unauthorized.value -> NetworkException.Companion.ERROR_UNAUTHORIZED
        HttpStatusCode.Companion.Forbidden.value -> NetworkException.Companion.ERROR_PERMISSION_DENIED
        HttpStatusCode.Companion.NotFound.value -> NetworkException.Companion.ERROR_NOT_FOUND
        HttpStatusCode.Companion.Conflict.value -> NetworkException.Companion.ERROR_CONFLICT
        HttpStatusCode.Companion.UnprocessableEntity.value -> NetworkException.Companion.ERROR_VALIDATION
        423 -> NetworkException.Companion.ERROR_LOCKED
        in 500..599 -> NetworkException.Companion.ERROR_INTERNAL_SERVER
        else -> NetworkException.Companion.ERROR_HTTP_EXCEPTION
    }

    fun mapToNetworkException(e: Exception): Throwable = when (e) {
        is NetworkException -> e
        is SocketTimeoutException -> NetworkException(NetworkException.Companion.ERROR_TIMEOUT)
        is PlatformIOException -> NetworkException(NetworkException.Companion.ERROR_NETWORK_IO)
        else -> NetworkException(NetworkException.Companion.ERROR_UNDEFINED)
    }
}
