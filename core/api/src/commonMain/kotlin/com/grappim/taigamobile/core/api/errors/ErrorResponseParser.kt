package com.grappim.taigamobile.core.api.errors

import com.grappim.taigamobile.core.api.HttpJson
import com.grappim.taigamobile.core.domain.ProjectLimitInfo
import com.grappim.taigamobile.core.domain.TaigaErrorDetails
import com.grappim.taigamobile.core.logger.LogPriority
import com.grappim.taigamobile.core.logger.logcat
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
class ErrorResponseParser(@param:HttpJson private val json: Json) {
    fun parseErrorResponse(
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
