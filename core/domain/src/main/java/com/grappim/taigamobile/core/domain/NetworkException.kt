package com.grappim.taigamobile.core.domain

import java.io.IOException

data class ProjectLimitInfo(val totalMemberships: Int, val isPrivate: Boolean)

data class TaigaErrorDetails(
    val message: String,
    val type: String?,
    val statusCode: Int,
    val fieldErrors: Map<String, List<String>>? = null,
    val projectLimitInfo: ProjectLimitInfo? = null
)

class NetworkException(val errorCode: Int, val request: String? = "", val taigaError: TaigaErrorDetails? = null) :
    IOException() {

    override val message: String?
        get() = taigaError?.message ?: "Error code: $errorCode"

    companion object {
        const val ERROR_API = -1
        const val ERROR_NO_INTERNET = -2
        const val ERROR_HOST_NOT_FOUND = -3
        const val ERROR_TIMEOUT = -4
        const val ERROR_NETWORK_IO = -5
        const val ERROR_UNDEFINED = -6
        const val ERROR_ON_REFRESH = -7
        const val ERROR_HTTP_EXCEPTION = -9
        const val ERROR_UNAUTHORIZED = -10
        const val ERROR_PERMISSION_DENIED = -11
        const val ERROR_NOT_FOUND = -12
        const val ERROR_BLOCKED = -13
        const val ERROR_VALIDATION = -14
        const val ERROR_METHOD_NOT_ALLOWED = -15
        const val ERROR_NOT_ACCEPTABLE = -16
        const val ERROR_UNSUPPORTED_MEDIA_TYPE = -17
        const val ERROR_THROTTLED = -18
        const val ERROR_INTERNAL_SERVER = -19
    }
}
