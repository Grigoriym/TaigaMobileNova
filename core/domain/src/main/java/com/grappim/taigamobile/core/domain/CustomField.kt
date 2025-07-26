package com.grappim.taigamobile.core.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate

@JsonClass(generateAdapter = false)
enum class CustomFieldType {
    @Json(name = "text")
    Text,

    @Json(name = "multiline")
    Multiline,

    @Json(name = "richtext")
    RichText,

    @Json(name = "date")
    Date,

    @Json(name = "url")
    Url,

    @Json(name = "dropdown")
    Dropdown,

    @Json(name = "number")
    Number,

    @Json(name = "checkbox")
    Checkbox
}

data class CustomField(
    val id: Long,
    val type: CustomFieldType,
    val name: String,
    val description: String? = null,
    val value: CustomFieldValue?,
    // for CustomFieldType.Dropdown
    val options: List<String>? = null
)

@JvmInline
value class CustomFieldValue(val value: Any) {
    init {
        require(
            value is String ||
                value is LocalDate ||
                value is Double ||
                value is Boolean
        )
    }

    val stringValue
        get() = value as? String ?: error("value is not String")
    val doubleValue get() = value as? Double ?: error("value is not Double")
    val dateValue get() = value as? LocalDate ?: error("value is not Date")
    val booleanValue
        get() = value as? Boolean ?: error("value is not Boolean")
}

data class CustomFields(val fields: List<CustomField>, val version: Long)
