package com.grappim.taigamobile.feature.workitem.domain.customfield

import java.time.LocalDate

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
