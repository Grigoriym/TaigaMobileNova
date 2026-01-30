package com.grappim.taigamobile.feature.workitem.mapper

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JsonObjectMapperTest {

    private lateinit var sut: JsonObjectMapper

    @Before
    fun setup() {
        sut = JsonObjectMapper()
    }

    @Test
    fun `toJsonObject should map null value to JsonNull`() {
        val map = mapOf<String, Any?>("key" to null)

        val result = sut.fromMapToJsonObject(map)

        assertEquals(JsonNull, result["key"])
    }

    @Test
    fun `toJsonObject should map Boolean true correctly`() {
        val map = mapOf<String, Any?>("enabled" to true)

        val result = sut.fromMapToJsonObject(map)

        assertEquals(JsonPrimitive(true), result["enabled"])
    }

    @Test
    fun `toJsonObject should map Boolean false correctly`() {
        val map = mapOf<String, Any?>("disabled" to false)

        val result = sut.fromMapToJsonObject(map)

        assertEquals(JsonPrimitive(false), result["disabled"])
    }

    @Test
    fun `toJsonObject should map Int correctly`() {
        val map = mapOf<String, Any?>("count" to 42)

        val result = sut.fromMapToJsonObject(map)

        assertEquals(JsonPrimitive(42), result["count"])
    }

    @Test
    fun `toJsonObject should map Long correctly`() {
        val map = mapOf<String, Any?>("id" to 123456789L)

        val result = sut.fromMapToJsonObject(map)

        assertEquals(JsonPrimitive(123456789L), result["id"])
    }

    @Test
    fun `toJsonObject should map Double correctly`() {
        val map = mapOf<String, Any?>("price" to 19.99)

        val result = sut.fromMapToJsonObject(map)

        assertEquals(JsonPrimitive(19.99), result["price"])
    }

    @Test
    fun `toJsonObject should map String correctly`() {
        val map = mapOf<String, Any?>("name" to "test value")

        val result = sut.fromMapToJsonObject(map)

        assertEquals(JsonPrimitive("test value"), result["name"])
    }

    @Test
    fun `toJsonObject should map empty String correctly`() {
        val map = mapOf<String, Any?>("empty" to "")

        val result = sut.fromMapToJsonObject(map)

        assertEquals(JsonPrimitive(""), result["empty"])
    }

    @Test
    fun `toJsonObject should map List of Strings correctly`() {
        val map = mapOf<String, Any?>("tags" to listOf("tag1", "tag2", "tag3"))

        val result = sut.fromMapToJsonObject(map)

        val array = result["tags"]!!.jsonArray
        assertEquals(3, array.size)
        assertEquals("tag1", array[0].jsonPrimitive.content)
        assertEquals("tag2", array[1].jsonPrimitive.content)
        assertEquals("tag3", array[2].jsonPrimitive.content)
    }

    @Test
    fun `toJsonObject should map List of Longs correctly`() {
        val map = mapOf<String, Any?>("watchers" to listOf(1L, 2L, 3L))

        val result = sut.fromMapToJsonObject(map)

        val array = result["watchers"]!!.jsonArray
        assertEquals(3, array.size)
        assertEquals(1L, array[0].jsonPrimitive.content.toLong())
        assertEquals(2L, array[1].jsonPrimitive.content.toLong())
        assertEquals(3L, array[2].jsonPrimitive.content.toLong())
    }

    @Test
    fun `toJsonObject should map List of Booleans correctly`() {
        val map = mapOf<String, Any?>("flags" to listOf(true, false, true))

        val result = sut.fromMapToJsonObject(map)

        val array = result["flags"]!!.jsonArray
        assertEquals(3, array.size)
        assertEquals(true, array[0].jsonPrimitive.content.toBoolean())
        assertEquals(false, array[1].jsonPrimitive.content.toBoolean())
        assertEquals(true, array[2].jsonPrimitive.content.toBoolean())
    }

    @Test
    fun `toJsonObject should map List with null values correctly`() {
        val map = mapOf<String, Any?>("items" to listOf("first", null, "third"))

        val result = sut.fromMapToJsonObject(map)

        val array = result["items"]!!.jsonArray
        assertEquals(3, array.size)
        assertEquals("first", array[0].jsonPrimitive.content)
        assertEquals(JsonNull, array[1])
        assertEquals("third", array[2].jsonPrimitive.content)
    }

    @Test
    fun `toJsonObject should map empty List correctly`() {
        val map = mapOf<String, Any?>("empty" to emptyList<Any>())

        val result = sut.fromMapToJsonObject(map)

        val array = result["empty"]!!.jsonArray
        assertTrue(array.isEmpty())
    }

    @Test
    fun `toJsonObject should map nested List correctly`() {
        val map = mapOf<String, Any?>("matrix" to listOf(listOf(1, 2), listOf(3, 4)))

        val result = sut.fromMapToJsonObject(map)

        val outerArray = result["matrix"]!!.jsonArray
        assertEquals(2, outerArray.size)
        val innerArray1 = outerArray[0].jsonArray
        val innerArray2 = outerArray[1].jsonArray
        assertEquals(2, innerArray1.size)
        assertEquals(2, innerArray2.size)
        assertEquals(1, innerArray1[0].jsonPrimitive.content.toInt())
        assertEquals(2, innerArray1[1].jsonPrimitive.content.toInt())
        assertEquals(3, innerArray2[0].jsonPrimitive.content.toInt())
        assertEquals(4, innerArray2[1].jsonPrimitive.content.toInt())
    }

    @Test
    fun `toJsonObject should map nested List with nulls correctly`() {
        val map = mapOf<String, Any?>("data" to listOf(listOf(null, "value")))

        val result = sut.fromMapToJsonObject(map)

        val outerArray = result["data"]!!.jsonArray
        val innerArray = outerArray[0].jsonArray
        assertEquals(JsonNull, innerArray[0])
        assertEquals("value", innerArray[1].jsonPrimitive.content)
    }

    @Test
    fun `toJsonObject should map nested Map correctly`() {
        val map = mapOf<String, Any?>(
            "user" to mapOf(
                "name" to "John",
                "age" to 30
            )
        )

        val result = sut.fromMapToJsonObject(map)

        val nestedObject = result["user"]!!.jsonObject
        assertEquals("John", nestedObject["name"]!!.jsonPrimitive.content)
        assertEquals(30, nestedObject["age"]!!.jsonPrimitive.content.toInt())
    }

    @Test
    fun `toJsonObject should map deeply nested Map correctly`() {
        val map = mapOf<String, Any?>(
            "level1" to mapOf(
                "level2" to mapOf(
                    "level3" to "deep value"
                )
            )
        )

        val result = sut.fromMapToJsonObject(map)

        val level1 = result["level1"]!!.jsonObject
        val level2 = level1["level2"]!!.jsonObject
        assertEquals("deep value", level2["level3"]!!.jsonPrimitive.content)
    }

    @Test
    fun `toJsonObject should map other types using toString`() {
        data class CustomObject(val value: Int)
        val map = mapOf<String, Any?>("custom" to CustomObject(42))

        val result = sut.fromMapToJsonObject(map)

        assertEquals("CustomObject(value=42)", result["custom"]!!.jsonPrimitive.content)
    }

    @Test
    fun `toJsonObject should map List with other types using toString`() {
        data class Item(val id: Int)
        val map = mapOf<String, Any?>("items" to listOf(Item(1), Item(2)))

        val result = sut.fromMapToJsonObject(map)

        val array = result["items"]!!.jsonArray
        assertEquals("Item(id=1)", array[0].jsonPrimitive.content)
        assertEquals("Item(id=2)", array[1].jsonPrimitive.content)
    }

    @Test
    fun `toJsonObject should map nested List with other types using toString`() {
        data class Point(val x: Int, val y: Int)
        val map = mapOf<String, Any?>("points" to listOf(listOf(Point(1, 2))))

        val result = sut.fromMapToJsonObject(map)

        val outerArray = result["points"]!!.jsonArray
        val innerArray = outerArray[0].jsonArray
        assertEquals("Point(x=1, y=2)", innerArray[0].jsonPrimitive.content)
    }

    @Test
    fun `toJsonObject should handle empty map`() {
        val map = emptyMap<String, Any?>()

        val result = sut.fromMapToJsonObject(map)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `toJsonObject should handle complex mixed map`() {
        val map = mapOf(
            "version" to 1L,
            "subject" to "Test Subject",
            "isBlocked" to false,
            "assignedTo" to null,
            "watchers" to listOf(1L, 2L, 3L),
            "tags" to listOf(listOf("tag1", "color1"), listOf("tag2", "color2")),
            "attributes_values" to mapOf(
                "1" to "value1",
                "2" to 42
            )
        )

        val result = sut.fromMapToJsonObject(map)

        assertEquals(1L, result["version"]!!.jsonPrimitive.content.toLong())
        assertEquals("Test Subject", result["subject"]!!.jsonPrimitive.content)
        assertEquals(false, result["isBlocked"]!!.jsonPrimitive.content.toBoolean())
        assertEquals(JsonNull, result["assignedTo"])
        assertEquals(3, result["watchers"]!!.jsonArray.size)
        assertEquals(2, result["tags"]!!.jsonArray.size)
        assertEquals("value1", result["attributes_values"]!!.jsonObject["1"]!!.jsonPrimitive.content)
        assertEquals(42, result["attributes_values"]!!.jsonObject["2"]!!.jsonPrimitive.content.toInt())
    }

    @Test
    fun `toJsonObject should handle nested List with Booleans correctly`() {
        val map = mapOf<String, Any?>("flags" to listOf(listOf(true, false)))

        val result = sut.fromMapToJsonObject(map)

        val outerArray = result["flags"]!!.jsonArray
        val innerArray = outerArray[0].jsonArray
        assertEquals(true, innerArray[0].jsonPrimitive.content.toBoolean())
        assertEquals(false, innerArray[1].jsonPrimitive.content.toBoolean())
    }

    @Test
    fun `toJsonObject should handle nested List with Strings correctly`() {
        val map = mapOf<String, Any?>("names" to listOf(listOf("Alice", "Bob")))

        val result = sut.fromMapToJsonObject(map)

        val outerArray = result["names"]!!.jsonArray
        val innerArray = outerArray[0].jsonArray
        assertEquals("Alice", innerArray[0].jsonPrimitive.content)
        assertEquals("Bob", innerArray[1].jsonPrimitive.content)
    }
}
