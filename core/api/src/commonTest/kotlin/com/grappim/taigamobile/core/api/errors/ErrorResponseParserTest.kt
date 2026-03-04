package com.grappim.taigamobile.core.api.errors

import com.grappim.taigamobile.core.domain.ProjectLimitInfo
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ErrorResponseParserTest {

    private val json = Json {
        isLenient = true
        prettyPrint = false
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private lateinit var sut: ErrorResponseParser

    @BeforeTest
    fun setup() {
        sut = ErrorResponseParser(json)
    }

    @Test
    fun `parseErrorResponse with errorMessage and errorType returns TaigaErrorDetails`() {
        val body = """{"_error_message":"Something went wrong","_error_type":"taiga.users.error"}"""

        val result = sut.parseErrorResponse(body, 400, null)

        assertEquals("Something went wrong", result?.message)
        assertEquals("taiga.users.error", result?.type)
        assertEquals(400, result?.statusCode)
        assertNull(result?.projectLimitInfo)
    }

    @Test
    fun `parseErrorResponse with detail and code uses them as fallback`() {
        val body = """{"detail":"Not found.","code":"not_found"}"""

        val result = sut.parseErrorResponse(body, 404, null)

        assertEquals("Not found.", result?.message)
        assertEquals("not_found", result?.type)
        assertEquals(404, result?.statusCode)
    }

    @Test
    fun `parseErrorResponse with errorMessage prefers errorMessage over detail`() {
        val body = """{"_error_message":"Primary",
            |"detail":"Secondary",
            |"_error_type":"primary_type",
            |"code":"secondary_code"}
        """.trimMargin()

        val result = sut.parseErrorResponse(body, 400, null)

        assertEquals("Primary", result?.message)
        assertEquals("primary_type", result?.type)
    }

    @Test
    fun `parseErrorResponse propagates projectLimitInfo`() {
        val body = """{"_error_message":"Limit reached",
            |"_error_type":"taiga.projects.project.members_limit_for_project"}
        """.trimMargin()
        val limitInfo = ProjectLimitInfo(totalMemberships = 5, isPrivate = true)

        val result = sut.parseErrorResponse(body, 400, limitInfo)

        assertEquals(limitInfo, result?.projectLimitInfo)
    }

    @Test
    fun `parseErrorResponse with no message fields falls through to validation error parsing`() {
        val body = """{"username":["This field is required."],"email":["Enter a valid email address."]}"""

        val result = sut.parseErrorResponse(body, 400, null)

        assertEquals("ValidationError", result?.type)
        assertEquals(400, result?.statusCode)
        assertEquals(
            mapOf(
                "username" to listOf("This field is required."),
                "email" to listOf("Enter a valid email address.")
            ),
            result?.fieldErrors
        )
    }

    @Test
    fun `parseErrorResponse with invalid JSON falls through to validation error and returns null`() {
        val body = "not json at all"

        val result = sut.parseErrorResponse(body, 500, null)

        assertNull(result)
    }

    @Test
    fun `parseErrorResponse with empty validation map returns null`() {
        val body = """{}"""

        val result = sut.parseErrorResponse(body, 400, null)

        assertNull(result)
    }

    @Test
    fun `parseErrorResponse validation message is formatted correctly`() {
        val body = """{"password":["Too short.","Too common."]}"""

        val result = sut.parseErrorResponse(body, 400, null)

        assertEquals("Password: Too short., Too common.", result?.message)
    }

    @Test
    fun `parseErrorResponse validation message capitalises field name`() {
        val body = """{"username":["Already exists."]}"""

        val result = sut.parseErrorResponse(body, 400, null)

        assertEquals("Username: Already exists.", result?.message)
    }
}
