package com.grappim.taigamobile.utils.ui

import androidx.savedstate.read
import androidx.savedstate.savedState
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.nullable
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Serializable
private data class Payload(val id: Long, val name: String)

/**
 * `serializeAsValue` goes through the `urlEncode` `expect` function, so the encoded strings asserted
 * here are the JVM actual's (`io.ktor.http.encodeURLParameter`). The Android actual is
 * `android.net.Uri.encode`, which escapes a different character set — those assertions are written
 * as round-trips through `parseValue` rather than against a literal wherever that difference could
 * show.
 */
class JsonSerializableNavTypeTest {

    private val payload = Payload(id = 42L, name = "a name")

    @Test
    fun `typeMapOf builds a nullable nav type for a nullable ktype`() {
        val type = typeOf<Payload?>()

        val navType = typeMapOf(listOf(type)).getValue(type)

        assertTrue(navType is JsonSerializableNullableNavType<*>)
        assertTrue(navType.isNullableAllowed)
    }

    @Test
    fun `typeMapOf builds a non-nullable nav type for a non-nullable ktype`() {
        val type = typeOf<Payload>()

        val navType = typeMapOf(listOf(type)).getValue(type)

        assertTrue(navType is JsonSerializableNavType<*>)
        assertTrue(!navType.isNullableAllowed)
    }

    @Test
    fun `typeMapOf keys every ktype it is given`() {
        val types = listOf(typeOf<Payload>(), typeOf<Payload?>())

        assertEquals(types.toSet(), typeMapOf(types).keys)
    }

    @Test
    fun `put and get round-trip through a SavedState`() {
        val sut = JsonSerializableNavType(Payload.serializer())
        val bundle = savedState()

        sut.put(bundle, "key", payload)

        assertEquals(payload, sut.get(bundle, "key"))
    }

    @Test
    fun `parseValue reverses serializeAsValue`() {
        val sut = JsonSerializableNavType(Payload.serializer())

        assertEquals(payload, sut.parseValue(urlDecode(sut.serializeAsValue(payload))))
    }

    @Test
    fun `serializeAsValue url-encodes the json`() {
        val sut = JsonSerializableNavType(Payload.serializer())

        val encoded = sut.serializeAsValue(payload)

        assertTrue('{' !in encoded, "expected the json braces to be escaped, got $encoded")
        assertTrue(' ' !in encoded, "expected the space to be escaped, got $encoded")
    }

    @Test
    fun `nullable put and get round-trip a value`() {
        val sut = JsonSerializableNullableNavType<Payload>(Payload.serializer().nullable)
        val bundle = savedState()

        sut.put(bundle, "key", payload)

        assertEquals(payload, sut.get(bundle, "key"))
    }

    @Test
    fun `nullable put writes null and get reads it back`() {
        val sut = JsonSerializableNullableNavType<Payload>(Payload.serializer().nullable)
        val bundle = savedState()

        sut.put(bundle, "key", null)

        assertTrue(bundle.read { isNull("key") })
        assertNull(sut.get(bundle, "key"))
    }

    @Test
    fun `nullable serializeAsValue writes the literal null`() {
        val sut = JsonSerializableNullableNavType<Payload>(Payload.serializer().nullable)

        assertEquals("null", sut.serializeAsValue(null))
    }

    @Test
    fun `nullable parseValue reads the literal null back`() {
        val sut = JsonSerializableNullableNavType<Payload>(Payload.serializer().nullable)

        assertNull(sut.parseValue("null"))
    }

    @Test
    fun `nullable parseValue reverses serializeAsValue for a value`() {
        val sut = JsonSerializableNullableNavType<Payload>(Payload.serializer().nullable)

        assertEquals(payload, sut.parseValue(urlDecode(sut.serializeAsValue(payload))))
    }
}
