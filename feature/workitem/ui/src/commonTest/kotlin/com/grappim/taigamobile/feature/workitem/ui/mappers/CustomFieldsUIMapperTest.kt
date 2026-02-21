package com.grappim.taigamobile.feature.workitem.ui.mappers

import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomField
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFieldType
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFieldValue
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFields
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CheckboxItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.DateItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.DropdownItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.MultilineTextItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.NumberItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.RichTextItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.TextItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.UrlItemState
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.nowLocalDate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import java.text.DecimalFormat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CustomFieldsUIMapperTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val decimalFormat = DecimalFormat("#.##")

    private lateinit var sut: CustomFieldsUIMapper

    @Before
    fun setUp() {
        sut = CustomFieldsUIMapper(
            ioDispatcher = testDispatcher,
            dfSimple = decimalFormat
        )
    }

    @Test
    fun `toUI with empty fields should return empty list`() = runTest {
        val customFields = CustomFields(
            fields = persistentListOf(),
            version = getRandomLong()
        )

        val actual = sut.toUI(customFields)

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `toUI with Text field should return TextItemState`() = runTest {
        val id = getRandomLong()
        val name = getRandomString()
        val description = getRandomString()
        val value = getRandomString()

        val customFields = CustomFields(
            fields = persistentListOf(
                CustomField(
                    id = id,
                    type = CustomFieldType.Text,
                    name = name,
                    description = description,
                    value = CustomFieldValue(value)
                )
            ),
            version = getRandomLong()
        )

        val actual = sut.toUI(customFields)

        assertEquals(1, actual.size)
        val item = assertIs<TextItemState>(actual[0])
        assertEquals(id, item.id)
        assertEquals(name, item.label)
        assertEquals(description, item.description)
        assertEquals(value, item.originalValue)
        assertEquals(value, item.currentValue)
    }

    @Test
    fun `toUI with Text field and null value should return empty string`() = runTest {
        val id = getRandomLong()
        val name = getRandomString()

        val customFields = CustomFields(
            fields = persistentListOf(
                CustomField(
                    id = id,
                    type = CustomFieldType.Text,
                    name = name,
                    value = null
                )
            ),
            version = getRandomLong()
        )

        val actual = sut.toUI(customFields)

        val item = assertIs<TextItemState>(actual[0])
        assertEquals("", item.originalValue)
        assertEquals("", item.currentValue)
    }

    @Test
    fun `toUI with Multiline field should return MultilineTextItemState`() = runTest {
        val id = getRandomLong()
        val name = getRandomString()
        val description = getRandomString()
        val value = "Line 1\nLine 2\nLine 3"

        val customFields = CustomFields(
            fields = persistentListOf(
                CustomField(
                    id = id,
                    type = CustomFieldType.Multiline,
                    name = name,
                    description = description,
                    value = CustomFieldValue(value)
                )
            ),
            version = getRandomLong()
        )

        val actual = sut.toUI(customFields)

        assertEquals(1, actual.size)
        val item = assertIs<MultilineTextItemState>(actual[0])
        assertEquals(id, item.id)
        assertEquals(name, item.label)
        assertEquals(description, item.description)
        assertEquals(value, item.originalValue)
        assertEquals(value, item.currentValue)
    }

    @Test
    fun `toUI with RichText field should return RichTextItemState`() = runTest {
        val id = getRandomLong()
        val name = getRandomString()
        val description = getRandomString()
        val value = "<p>Rich <b>text</b> content</p>"

        val customFields = CustomFields(
            fields = persistentListOf(
                CustomField(
                    id = id,
                    type = CustomFieldType.RichText,
                    name = name,
                    description = description,
                    value = CustomFieldValue(value)
                )
            ),
            version = getRandomLong()
        )

        val actual = sut.toUI(customFields)

        assertEquals(1, actual.size)
        val item = assertIs<RichTextItemState>(actual[0])
        assertEquals(id, item.id)
        assertEquals(name, item.label)
        assertEquals(description, item.description)
        assertEquals(value, item.originalValue)
        assertEquals(value, item.currentValue)
    }

    @Test
    fun `toUI with Number field should return NumberItemState with formatted value`() = runTest {
        val id = getRandomLong()
        val name = getRandomString()
        val description = getRandomString()
        val value = 123.456

        val customFields = CustomFields(
            fields = persistentListOf(
                CustomField(
                    id = id,
                    type = CustomFieldType.Number,
                    name = name,
                    description = description,
                    value = CustomFieldValue(value)
                )
            ),
            version = getRandomLong()
        )

        val actual = sut.toUI(customFields)

        assertEquals(1, actual.size)
        val item = assertIs<NumberItemState>(actual[0])
        assertEquals(id, item.id)
        assertEquals(name, item.label)
        assertEquals(description, item.description)
        assertEquals(decimalFormat.format(value), item.originalValue)
        assertEquals(decimalFormat.format(value), item.currentValue)
    }

    @Test
    fun `toUI with Number field and null value should return zero formatted`() = runTest {
        val id = getRandomLong()
        val name = getRandomString()

        val customFields = CustomFields(
            fields = persistentListOf(
                CustomField(
                    id = id,
                    type = CustomFieldType.Number,
                    name = name,
                    value = null
                )
            ),
            version = getRandomLong()
        )

        val actual = sut.toUI(customFields)

        val item = assertIs<NumberItemState>(actual[0])
        assertEquals(decimalFormat.format(0.0), item.originalValue)
        assertEquals(decimalFormat.format(0.0), item.currentValue)
    }

    @Test
    fun `toUI with Url field should return UrlItemState`() = runTest {
        val id = getRandomLong()
        val name = getRandomString()
        val description = getRandomString()
        val value = "https://example.com"

        val customFields = CustomFields(
            fields = persistentListOf(
                CustomField(
                    id = id,
                    type = CustomFieldType.Url,
                    name = name,
                    description = description,
                    value = CustomFieldValue(value)
                )
            ),
            version = getRandomLong()
        )

        val actual = sut.toUI(customFields)

        assertEquals(1, actual.size)
        val item = assertIs<UrlItemState>(actual[0])
        assertEquals(id, item.id)
        assertEquals(name, item.label)
        assertEquals(description, item.description)
        assertEquals(value, item.originalValue)
        assertEquals(value, item.currentValue)
    }

    @Test
    fun `toUI with Date field should return DateItemState`() = runTest {
        val id = getRandomLong()
        val name = getRandomString()
        val description = getRandomString()
        val value = nowLocalDate

        val customFields = CustomFields(
            fields = persistentListOf(
                CustomField(
                    id = id,
                    type = CustomFieldType.Date,
                    name = name,
                    description = description,
                    value = CustomFieldValue(value)
                )
            ),
            version = getRandomLong()
        )

        val actual = sut.toUI(customFields)

        assertEquals(1, actual.size)
        val item = assertIs<DateItemState>(actual[0])
        assertEquals(id, item.id)
        assertEquals(name, item.label)
        assertEquals(description, item.description)
        assertEquals(value, item.originalValue)
        assertEquals(value, item.currentValue)
    }

    @Test
    fun `toUI with Date field and null value should return null dates`() = runTest {
        val id = getRandomLong()
        val name = getRandomString()

        val customFields = CustomFields(
            fields = persistentListOf(
                CustomField(
                    id = id,
                    type = CustomFieldType.Date,
                    name = name,
                    value = null
                )
            ),
            version = getRandomLong()
        )

        val actual = sut.toUI(customFields)

        val item = assertIs<DateItemState>(actual[0])
        assertEquals(null, item.originalValue)
        assertEquals(null, item.currentValue)
    }

    @Test
    fun `toUI with Checkbox field should return CheckboxItemState`() = runTest {
        val id = getRandomLong()
        val name = getRandomString()
        val description = getRandomString()
        val value = true

        val customFields = CustomFields(
            fields = persistentListOf(
                CustomField(
                    id = id,
                    type = CustomFieldType.Checkbox,
                    name = name,
                    description = description,
                    value = CustomFieldValue(value)
                )
            ),
            version = getRandomLong()
        )

        val actual = sut.toUI(customFields)

        assertEquals(1, actual.size)
        val item = assertIs<CheckboxItemState>(actual[0])
        assertEquals(id, item.id)
        assertEquals(name, item.label)
        assertEquals(description, item.description)
        assertEquals(value, item.originalValue)
        assertEquals(value, item.currentValue)
    }

    @Test
    fun `toUI with Checkbox field and null value should return false`() = runTest {
        val id = getRandomLong()
        val name = getRandomString()

        val customFields = CustomFields(
            fields = persistentListOf(
                CustomField(
                    id = id,
                    type = CustomFieldType.Checkbox,
                    name = name,
                    value = null
                )
            ),
            version = getRandomLong()
        )

        val actual = sut.toUI(customFields)

        val item = assertIs<CheckboxItemState>(actual[0])
        assertEquals(false, item.originalValue)
        assertEquals(false, item.currentValue)
    }

    @Test
    fun `toUI with Dropdown field should return DropdownItemState`() = runTest {
        val id = getRandomLong()
        val name = getRandomString()
        val description = getRandomString()
        val options = persistentListOf("Option 1", "Option 2", "Option 3")
        val value = "Option 2"

        val customFields = CustomFields(
            fields = persistentListOf(
                CustomField(
                    id = id,
                    type = CustomFieldType.Dropdown,
                    name = name,
                    description = description,
                    value = CustomFieldValue(value),
                    options = options
                )
            ),
            version = getRandomLong()
        )

        val actual = sut.toUI(customFields)

        assertEquals(1, actual.size)
        val item = assertIs<DropdownItemState>(actual[0])
        assertEquals(id, item.id)
        assertEquals(name, item.label)
        assertEquals(description, item.description)
        assertEquals(options, item.options)
        assertEquals(value, item.originalValue)
        assertEquals(value, item.currentValue)
    }

    @Test
    fun `toUI with Dropdown field and null value should return null selected`() = runTest {
        val id = getRandomLong()
        val name = getRandomString()
        val options = persistentListOf("Option 1", "Option 2")

        val customFields = CustomFields(
            fields = persistentListOf(
                CustomField(
                    id = id,
                    type = CustomFieldType.Dropdown,
                    name = name,
                    value = null,
                    options = options
                )
            ),
            version = getRandomLong()
        )

        val actual = sut.toUI(customFields)

        val item = assertIs<DropdownItemState>(actual[0])
        assertEquals(null, item.originalValue)
        assertEquals(null, item.currentValue)
    }

    @Test
    fun `toUI with multiple fields should return correct item types`() = runTest {
        val customFields = CustomFields(
            fields = persistentListOf(
                CustomField(
                    id = 1L,
                    type = CustomFieldType.Text,
                    name = "Text Field",
                    value = CustomFieldValue("text")
                ),
                CustomField(
                    id = 2L,
                    type = CustomFieldType.Number,
                    name = "Number Field",
                    value = CustomFieldValue(42.0)
                ),
                CustomField(
                    id = 3L,
                    type = CustomFieldType.Checkbox,
                    name = "Checkbox Field",
                    value = CustomFieldValue(true)
                )
            ),
            version = getRandomLong()
        )

        val actual = sut.toUI(customFields)

        assertEquals(3, actual.size)
        assertIs<TextItemState>(actual[0])
        assertIs<NumberItemState>(actual[1])
        assertIs<CheckboxItemState>(actual[2])
    }
}
