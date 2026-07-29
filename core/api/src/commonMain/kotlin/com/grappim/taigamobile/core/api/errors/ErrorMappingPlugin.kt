package com.grappim.taigamobile.core.api.errors

import com.grappim.taigamobile.core.domain.NetworkException
import com.grappim.taigamobile.core.domain.ProjectLimitInfo
import com.grappim.taigamobile.core.logger.LogPriority
import com.grappim.taigamobile.core.logger.logcat
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.util.AttributeKey
import kotlinx.serialization.json.Json

class ErrorMappingPlugin(
    private val json: Json,
    private val networkErrorMapper: NetworkErrorMapper,
    private val errorResponseParser: ErrorResponseParser
) {
    class Config {
        lateinit var json: Json
        lateinit var errorMapper: NetworkErrorMapper
        lateinit var errorResponseParser: ErrorResponseParser
    }

    companion object Plugin : HttpClientPlugin<Config, ErrorMappingPlugin> {
        override val key: AttributeKey<ErrorMappingPlugin> =
            io.ktor.util.AttributeKey("ErrorMappingPlugin")

        override fun prepare(block: Config.() -> Unit): ErrorMappingPlugin {
            val config = Config().apply(block)
            return ErrorMappingPlugin(
                json = config.json,
                networkErrorMapper = config.errorMapper,
                errorResponseParser = config.errorResponseParser
            )
        }

        override fun install(plugin: ErrorMappingPlugin, scope: HttpClient) {
            scope.plugin(HttpSend.Plugin).intercept { request ->
                try {
                    val response = execute(request)

                    if (response.response.status.value >= 400) {
                        val errorBody = response.response.bodyAsText()
                        val statusCode = response.response.status.value
                        val requestUrl = request.url.toString()

                        val projectLimitInfo = extractProjectLimitInfo(response.response)
                        val taigaError = plugin.errorResponseParser.parseErrorResponse(
                            errorBody = errorBody,
                            statusCode = statusCode,
                            projectLimitInfo = projectLimitInfo
                        )
                        val networkErrorCode = plugin.networkErrorMapper.mapToErrorCode(statusCode)

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
                    logcat(priority = LogPriority.ERROR, tag = "ErrorMappingPlugin", throwable = e) {
                        "Request failed"
                    }
                    throw plugin.networkErrorMapper.mapToNetworkException(e)
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
    }
}
