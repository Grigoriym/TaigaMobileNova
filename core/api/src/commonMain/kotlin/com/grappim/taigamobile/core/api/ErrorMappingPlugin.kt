package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.domain.NetworkException
import com.grappim.taigamobile.core.domain.PlatformIOException
import com.grappim.taigamobile.core.domain.ProjectLimitInfo
import com.grappim.taigamobile.core.domain.TaigaErrorDetails
import com.grappim.taigamobile.core.logger.LogPriority
import com.grappim.taigamobile.core.logger.logcat
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.util.AttributeKey
import kotlinx.serialization.json.Json

class ErrorMappingPlugin(
    private val json: Json
) {
    class Config {
        lateinit var json: Json
    }

    companion object Plugin : HttpClientPlugin<Config, ErrorMappingPlugin> {
        override val key: AttributeKey<ErrorMappingPlugin> = AttributeKey("ErrorMappingPlugin")

        override fun prepare(block: Config.() -> Unit): ErrorMappingPlugin {
            val config = Config().apply(block)
            return ErrorMappingPlugin(config.json)
        }

        override fun install(plugin: ErrorMappingPlugin, scope: HttpClient) {
            scope.plugin(HttpSend).intercept { request ->
                try {
                    val response = execute(request)

                    if (response.response.status.value >= 400) {
                        val errorBody = response.response.bodyAsText()
                        val statusCode = response.response.status.value
                        val requestUrl = request.url.toString()

                        val projectLimitInfo = extractProjectLimitInfo(response.response)
                        val taigaError = plugin.parseErrorResponse(errorBody, statusCode, projectLimitInfo)
                        val networkErrorCode = statusCode.mapToErrorCode()

                        throw NetworkException(
                            errorCode = networkErrorCode,
                            request = requestUrl,
                            taigaError = taigaError
                        )
                    }

                    response
                } catch (e: NetworkException) {
                    throw e
                } catch (e: Exception) {
                    throw e.mapToNetworkException()
                }
            }
        }

        private fun extractProjectLimitInfo(response: HttpResponse): ProjectLimitInfo? {
            val memberships = response.headers["Taiga-Info-Project-Memberships"]?.toIntOrNull()
            val isPrivate = response.headers["Taiga-Info-Project-Is-Private"]?.toBooleanStrictOrNull()

            return if (memberships != null && isPrivate != null) {
                ProjectLimitInfo(memberships, isPrivate)
            } else {
                null
            }
        }

        private fun Int.mapToErrorCode(): Int = when (this) {
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

        private fun Exception.mapToNetworkException(): Throwable = when (this) {
            is NetworkException -> this
            is SocketTimeoutException -> NetworkException(NetworkException.ERROR_TIMEOUT)
            is PlatformIOException -> NetworkException(NetworkException.ERROR_NETWORK_IO)
            else -> NetworkException(NetworkException.ERROR_UNDEFINED)
        }
    }

    private fun parseErrorResponse(
        errorBody: String,
        statusCode: Int,
        projectLimitInfo: ProjectLimitInfo?
    ): TaigaErrorDetails? = try {
        val taigaError = json.decodeFromString<TaigaErrorResponse>(errorBody)
        val errorMessage = taigaError.errorMessage ?: taigaError.detail
        if (errorMessage != null) {
            TaigaErrorDetails(
                message = errorMessage,
                type = taigaError.errorType ?: taigaError.code,
                statusCode = statusCode,
                projectLimitInfo = projectLimitInfo
            )
        } else {
            parseValidationError(errorBody, statusCode)
        }
    } catch (e: Exception) {
        logcat(priority = LogPriority.ERROR, tag = "ErrorMappingPlugin", throwable = e) {
            "Failed to parse error response"
        }
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
        logcat(priority = LogPriority.WARN, tag = "ErrorMappingPlugin", throwable = e) {
            "Failed to parse validation error response"
        }
        null
    }
}