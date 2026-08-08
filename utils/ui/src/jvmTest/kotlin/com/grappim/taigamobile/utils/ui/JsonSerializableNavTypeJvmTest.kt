package com.grappim.taigamobile.utils.ui

import io.ktor.http.decodeURLPart
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.nullable
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
private data class JvmPayload(val id: Long, val name: String)

/**
 * `parseValue` never URL-decodes on its own — Navigation has already decoded the argument by the
 * time it reaches `parseValue`. This proves the full round trip anyway, by doing that decode step
 * explicitly with the JVM target's own decoder (`io.ktor.http.decodeURLPart`, the same function
 * `JsonSerializableNavType.jvm.kt`'s `urlEncode` actual pairs with), rather than assuming it away.
 */
class JsonSerializableNavTypeJvmTest {

    private val payload = JvmPayload(id = 42L, name = "a name")

    @Test
    fun `parseValue reverses serializeAsValue once the caller url-decodes`() {
        val sut = JsonSerializableNavType(JvmPayload.serializer())

        val decoded = sut.serializeAsValue(payload).decodeURLPart()

        assertEquals(payload, sut.parseValue(decoded))
    }

    @Test
    fun `nullable parseValue reverses serializeAsValue once the caller url-decodes`() {
        val sut = JsonSerializableNullableNavType<JvmPayload>(JvmPayload.serializer().nullable)

        val decoded = sut.serializeAsValue(payload).decodeURLPart()

        assertEquals(payload, sut.parseValue(decoded))
    }
}
