package com.grappim.taigamobile.data.interceptors

import com.grappim.taigamobile.core.domain.NetworkException
import com.grappim.taigamobile.data.api.ConnectivityManagerNetworkMonitor
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorMappingInterceptor @Inject constructor(
    private val connectivityManagerNetworkMonitor: ConnectivityManagerNetworkMonitor
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!connectivityManagerNetworkMonitor.isOnline) {
            throw NetworkException(NetworkException.ERROR_NO_INTERNET)
        }
        val request = chain.request()
        try {
            val response = chain.proceed(request)
            if (response.isSuccessful) return response
            if (response.isPaginationEnd()) {
                throw NetworkException(
                    NetworkException.ERROR_404_PAGINATION
                )
            }
            return response
        } catch (e: Exception) {
            Timber.e(t = e, message = "ErrorMappingInterceptor")
            throw e.mapNetworkException()
        }
    }

    private fun Exception.mapNetworkException(): Throwable = when (this) {
        is NetworkException -> this
        is SocketTimeoutException -> NetworkException(NetworkException.ERROR_TIMEOUT)
        is UnknownHostException -> NetworkException(NetworkException.ERROR_HOST_NOT_FOUND)
        is IOException -> NetworkException(NetworkException.ERROR_NETWORK_IO)
        else -> NetworkException(NetworkException.ERROR_UNDEFINED)
    }

    private fun Response.isPaginationEnd(): Boolean =
        code == 404 && body.string().contains("Page is not 'last'")
}
