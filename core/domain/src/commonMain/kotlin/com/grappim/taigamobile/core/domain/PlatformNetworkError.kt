package com.grappim.taigamobile.core.domain

sealed class PlatformNetworkError {
    data class Code(val errorCode: Int) : PlatformNetworkError()
    data class UntrustedCertificate(val pendingCertTrust: PendingCertTrust) : PlatformNetworkError()
}

expect fun mapPlatformNetworkError(exception: Exception): PlatformNetworkError?
