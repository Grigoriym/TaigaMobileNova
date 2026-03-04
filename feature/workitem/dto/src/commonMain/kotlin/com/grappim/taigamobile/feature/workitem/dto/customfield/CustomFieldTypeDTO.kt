package com.grappim.taigamobile.feature.workitem.dto.customfield

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CustomFieldTypeDTO {
    @SerialName(value = "text")
    Text,

    @SerialName(value = "multiline")
    Multiline,

    @SerialName(value = "richtext")
    RichText,

    @SerialName(value = "date")
    Date,

    @SerialName(value = "url")
    Url,

    @SerialName(value = "dropdown")
    Dropdown,

    @SerialName(value = "number")
    Number,

    @SerialName(value = "checkbox")
    Checkbox
}
