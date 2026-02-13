package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFieldType
import com.grappim.taigamobile.feature.workitem.dto.customattribute.CustomAttributeResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.customattribute.CustomAttributesValuesResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.customfield.CustomFieldTypeDTO
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CustomFieldsMapperTest {

    private val dateTimeUtils: DateTimeUtils = mockk()

    private lateinit var sut: CustomFieldsMapper

    @Before
    fun setup() {
        sut = CustomFieldsMapper(
            dateTimeUtils = dateTimeUtils
        )
    }

    @Test
    fun `toDomain should map version correctly`() {
        val version = getRandomLong()
        val values = CustomAttributesValuesResponseDTO(
            attributesValues = emptyMap(),
            version = version
        )

        val result = sut.toDomain(attributes = emptyList(), values = values)

        assertEquals(version, result.version)
    }

    @Test
    fun `toDomain should sort fields by order`() {
        val attr1 = createAttribute(id = 1L, order = 3L)
        val attr2 = createAttribute(id = 2L, order = 1L)
        val attr3 = createAttribute(id = 3L, order = 2L)
        val values = CustomAttributesValuesResponseDTO(
            attributesValues = emptyMap(),
            version = getRandomLong()
        )

        val result = sut.toDomain(attributes = listOf(attr1, attr2, attr3), values = values)

        assertEquals(3, result.fields.size)
        assertEquals(2L, result.fields[0].id)
        assertEquals(3L, result.fields[1].id)
        assertEquals(1L, result.fields[2].id)
    }

    @Test
    fun `toDomain should map basic field properties`() {
        val id = getRandomLong()
        val name = getRandomString()
        val description = getRandomString()
        val attr = createAttribute(id = id, name = name, description = description)
        val values = CustomAttributesValuesResponseDTO(
            attributesValues = emptyMap(),
            version = getRandomLong()
        )

        val result = sut.toDomain(values = values, resp = attr)

        assertEquals(id, result.id)
        assertEquals(name, result.name)
        assertEquals(description, result.description)
    }

    @Test
    fun `toDomain should set description to null when empty`() {
        val attr = createAttribute(description = "")
        val values = CustomAttributesValuesResponseDTO(
            attributesValues = emptyMap(),
            version = getRandomLong()
        )

        val result = sut.toDomain(values = values, resp = attr)

        assertNull(result.description)
    }

    @Test
    fun `toDomain should set description to null when null`() {
        val attr = createAttribute(description = null)
        val values = CustomAttributesValuesResponseDTO(
            attributesValues = emptyMap(),
            version = getRandomLong()
        )

        val result = sut.toDomain(values = values, resp = attr)

        assertNull(result.description)
    }

    @Test
    fun `toDomain should map Text type correctly`() {
        val textValue = getRandomString()
        val attr = createAttribute(id = 1L, type = CustomFieldTypeDTO.Text)
        val values = CustomAttributesValuesResponseDTO(
            attributesValues = mapOf("1" to JsonPrimitive(textValue)),
            version = getRandomLong()
        )

        val result = sut.toDomain(values = values, resp = attr)

        assertEquals(CustomFieldType.Text, result.type)
        assertEquals(textValue, result.value?.value)
    }

    @Test
    fun `toDomain should map Multiline type correctly`() {
        val attr = createAttribute(type = CustomFieldTypeDTO.Multiline)
        val values = CustomAttributesValuesResponseDTO(
            attributesValues = emptyMap(),
            version = getRandomLong()
        )

        val result = sut.toDomain(values = values, resp = attr)

        assertEquals(CustomFieldType.Multiline, result.type)
    }

    @Test
    fun `toDomain should map RichText type correctly`() {
        val attr = createAttribute(type = CustomFieldTypeDTO.RichText)
        val values = CustomAttributesValuesResponseDTO(
            attributesValues = emptyMap(),
            version = getRandomLong()
        )

        val result = sut.toDomain(values = values, resp = attr)

        assertEquals(CustomFieldType.RichText, result.type)
    }

    @Test
    fun `toDomain should map Date type correctly`() {
        val dateString = "2024-12-31"
        val expectedDate = LocalDate.of(2024, 12, 31)
        val attr = createAttribute(id = 1L, type = CustomFieldTypeDTO.Date)
        val values = CustomAttributesValuesResponseDTO(
            attributesValues = mapOf("1" to JsonPrimitive(dateString)),
            version = getRandomLong()
        )

        every { dateTimeUtils.parseToLocalDate(dateString) } returns expectedDate

        val result = sut.toDomain(values = values, resp = attr)

        assertEquals(CustomFieldType.Date, result.type)
        assertEquals(expectedDate, result.value?.value)
    }

    @Test
    fun `toDomain should map Url type correctly`() {
        val urlValue = "https://example.com"
        val attr = createAttribute(id = 1L, type = CustomFieldTypeDTO.Url)
        val values = CustomAttributesValuesResponseDTO(
            attributesValues = mapOf("1" to JsonPrimitive(urlValue)),
            version = getRandomLong()
        )

        val result = sut.toDomain(values = values, resp = attr)

        assertEquals(CustomFieldType.Url, result.type)
        assertEquals(urlValue, result.value?.value)
    }

    @Test
    fun `toDomain should map Dropdown type correctly`() {
        val dropdownValue = "option1"
        val attr = createAttribute(id = 1L, type = CustomFieldTypeDTO.Dropdown)
        val values = CustomAttributesValuesResponseDTO(
            attributesValues = mapOf("1" to JsonPrimitive(dropdownValue)),
            version = getRandomLong()
        )

        val result = sut.toDomain(values = values, resp = attr)

        assertEquals(CustomFieldType.Dropdown, result.type)
        assertEquals(dropdownValue, result.value?.value)
    }

    @Test
    fun `toDomain should map Number type correctly`() {
        val numberValue = 42.5
        val attr = createAttribute(id = 1L, type = CustomFieldTypeDTO.Number)
        val values = CustomAttributesValuesResponseDTO(
            attributesValues = mapOf("1" to JsonPrimitive(numberValue)),
            version = getRandomLong()
        )

        val result = sut.toDomain(values = values, resp = attr)

        assertEquals(CustomFieldType.Number, result.type)
        assertEquals(numberValue, result.value?.value)
    }

    @Test
    fun `toDomain should map Checkbox type correctly`() {
        val attr = createAttribute(id = 1L, type = CustomFieldTypeDTO.Checkbox)
        val values = CustomAttributesValuesResponseDTO(
            attributesValues = mapOf("1" to JsonPrimitive(true)),
            version = getRandomLong()
        )

        val result = sut.toDomain(values = values, resp = attr)

        assertEquals(CustomFieldType.Checkbox, result.type)
        assertEquals(true, result.value?.value)
    }

    @Test
    fun `toDomain should return null value when attribute not in values map`() {
        val attr = createAttribute(id = 1L)
        val values = CustomAttributesValuesResponseDTO(
            attributesValues = emptyMap(),
            version = getRandomLong()
        )

        val result = sut.toDomain(values = values, resp = attr)

        assertNull(result.value)
    }

    @Test
    fun `toDomain should return null value for empty date string`() {
        val attr = createAttribute(id = 1L, type = CustomFieldTypeDTO.Date)
        val values = CustomAttributesValuesResponseDTO(
            attributesValues = mapOf("1" to JsonPrimitive("")),
            version = getRandomLong()
        )

        val result = sut.toDomain(values = values, resp = attr)

        assertNull(result.value)
    }

    @Test
    fun `toDomain should map extra options correctly`() {
        val options = listOf("option1", "option2", "option3").toImmutableList()
        val attr = createAttribute(extra = options)
        val values = CustomAttributesValuesResponseDTO(
            attributesValues = emptyMap(),
            version = getRandomLong()
        )

        val result = sut.toDomain(values = values, resp = attr)

        assertEquals(options, result.options)
    }

    @Test
    fun `toDomain should handle null extra options`() {
        val attr = createAttribute(extra = null)
        val values = CustomAttributesValuesResponseDTO(
            attributesValues = emptyMap(),
            version = getRandomLong()
        )

        val result = sut.toDomain(values = values, resp = attr)

        assertEquals(result.options?.isEmpty(), true)
    }

    private fun createAttribute(
        id: Long = getRandomLong(),
        name: String = getRandomString(),
        description: String? = getRandomString(),
        order: Long = getRandomLong(),
        type: CustomFieldTypeDTO = CustomFieldTypeDTO.Text,
        extra: List<String>? = null
    ) = CustomAttributeResponseDTO(
        id = id,
        name = name,
        description = description,
        order = order,
        type = type,
        extra = extra
    )
}
