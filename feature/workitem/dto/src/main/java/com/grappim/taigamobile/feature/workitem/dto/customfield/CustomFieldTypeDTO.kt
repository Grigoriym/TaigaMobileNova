package com.grappim.taigamobile.feature.workitem.dto.customfield

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class CustomFieldTypeDTO {
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
