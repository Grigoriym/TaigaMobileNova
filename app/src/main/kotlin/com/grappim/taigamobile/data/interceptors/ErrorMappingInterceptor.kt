package com.grappim.taigamobile.data.interceptors

import com.grappim.taigamobile.core.api.TaigaErrorResponse
import com.grappim.taigamobile.core.domain.NetworkException
import com.grappim.taigamobile.core.domain.TaigaErrorDetails
import com.grappim.taigamobile.data.api.ConnectivityManagerNetworkMonitor
import kotlinx.serialization.json.Json
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
    private val connectivityManagerNetworkMonitor: ConnectivityManagerNetworkMonitor,
    private val json: Json
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!connectivityManagerNetworkMonitor.isOnline) {
            throw NetworkException(NetworkException.ERROR_NO_INTERNET)
        }
        val request = chain.request()
        try {
            val response = chain.proceed(request)
            if (response.isSuccessful) return response

            val errorCode = response.code
            val errorBody = response.body.string()
            val requestUrl = request.url.toString()

            val projectLimitInfo = extractProjectLimitInfo(response)
            val taigaError = parseErrorResponse(errorBody, errorCode, projectLimitInfo)
            val networkErrorCode = errorCode.mapToErrorCode()

            throw NetworkException(
                errorCode = networkErrorCode,
                request = requestUrl,
                taigaError = taigaError
            )
        } catch (e: NetworkException) {
            throw e
        } catch (e: Exception) {
            Timber.e(t = e, message = "ErrorMappingInterceptor")
            throw e.mapNetworkException()
        }
    }

    private fun extractProjectLimitInfo(response: Response): com.grappim.taigamobile.core.domain.ProjectLimitInfo? {
        val memberships = response.header("Taiga-Info-Project-Memberships")?.toIntOrNull()
        val isPrivate = response.header("Taiga-Info-Project-Is-Private")?.toBooleanStrictOrNull()

        return if (memberships != null && isPrivate != null) {
            com.grappim.taigamobile.core.domain.ProjectLimitInfo(memberships, isPrivate)
        } else {
            null
        }
    }

    private fun parseErrorResponse(
        errorBody: String,
        statusCode: Int,
        projectLimitInfo: com.grappim.taigamobile.core.domain.ProjectLimitInfo?
    ): TaigaErrorDetails? = try {
        val taigaError = json.decodeFromString<TaigaErrorResponse>(errorBody)
        val errorMessage = taigaError.errorMessage
        if (errorMessage != null) {
            TaigaErrorDetails(
                message = errorMessage,
                type = taigaError.errorType,
                statusCode = statusCode,
                projectLimitInfo = projectLimitInfo
            )
        } else {
            parseValidationError(errorBody, statusCode)
        }
    } catch (e: Exception) {
        Timber.e(e)
        parseValidationError(errorBody, statusCode)
    }

    private fun parseValidationError(errorBody: String, statusCode: Int): TaigaErrorDetails? = try {
        val fieldErrors: Map<String, List<String>> = json.decodeFromString(errorBody)

        if (fieldErrors.isNotEmpty()) {
            val message = fieldErrors.entries.joinToString("; ") { (field, errors) ->
                val fieldName = field.replaceFirstChar { it.uppercase() }
                "$fieldName: ${errors.joinToString(", ")}"
            }

            TaigaErrorDetails(
                message = message,
                type = "ValidationError",
                statusCode = statusCode,
                fieldErrors = fieldErrors
            )
        } else {
            null
        }
    } catch (e: Exception) {
        Timber.w(e, "Failed to parse validation error response")
        null
    }

    private fun Int.mapToErrorCode(): Int = when (this) {
        400 -> NetworkException.ERROR_VALIDATION
        401 -> NetworkException.ERROR_UNAUTHORIZED
        403 -> NetworkException.ERROR_PERMISSION_DENIED
        404 -> NetworkException.ERROR_NOT_FOUND
        405 -> NetworkException.ERROR_METHOD_NOT_ALLOWED
        406 -> NetworkException.ERROR_NOT_ACCEPTABLE
        415 -> NetworkException.ERROR_UNSUPPORTED_MEDIA_TYPE
        429 -> NetworkException.ERROR_THROTTLED
        451 -> NetworkException.ERROR_BLOCKED
        500 -> NetworkException.ERROR_INTERNAL_SERVER
        else -> NetworkException.ERROR_HTTP_EXCEPTION
    }

    private fun Exception.mapNetworkException(): Throwable = when (this) {
        is NetworkException -> this
        is SocketTimeoutException -> NetworkException(NetworkException.ERROR_TIMEOUT)
        is UnknownHostException -> NetworkException(NetworkException.ERROR_HOST_NOT_FOUND)
        is IOException -> NetworkException(NetworkException.ERROR_NETWORK_IO)
        else -> NetworkException(NetworkException.ERROR_UNDEFINED)
    }
}
