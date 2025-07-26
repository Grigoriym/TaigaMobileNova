package com.grappim.taigamobile.feature.workitem.ui.widgets.customfields

import kotlinx.collections.immutable.ImmutableList
import java.time.LocalDate

sealed interface CustomFieldItemState {
    val id: Long
    val label: String
    val description: String?
    val isModified: Boolean

    fun getValueForPatching(): Any?
    fun getSavedItem(): CustomFieldItemState
}

/**
 * Interface for items that are edited by clicking on the edit button,
 * e.g. a RichText, url item, which has a separate edit mode.
 */
interface EditableItem

data class TextItemState(
    override val id: Long,
    override val description: String?,
    override val label: String,
    val originalValue: String,
    val currentValue: String = originalValue
) : CustomFieldItemState {
    override val isModified: Boolean
        get() = originalValue != currentValue

    override fun getValueForPatching(): Any? = currentValue

    override fun getSavedItem(): CustomFieldItemState = copy(originalValue = currentValue)
}

data class MultilineTextItemState(
    override val id: Long,
    override val description: String?,
    override val label: String,
    val originalValue: String,
    val currentValue: String = originalValue
) : CustomFieldItemState {
    override val isModified: Boolean
        get() = originalValue != currentValue

    override fun getValueForPatching(): Any? = currentValue

    override fun getSavedItem(): CustomFieldItemState = copy(originalValue = currentValue)
}

data class RichTextItemState(
    override val id: Long,
    override val description: String?,
    override val label: String,
    val originalValue: String,
    val currentValue: String = originalValue
) : CustomFieldItemState,
    EditableItem {
    override val isModified: Boolean
        get() = originalValue != currentValue

    override fun getValueForPatching(): Any? = currentValue

    override fun getSavedItem(): CustomFieldItemState = copy(originalValue = currentValue)
}

data class NumberItemState(
    override val id: Long,
    override val description: String?,
    override val label: String,
    val originalValue: String,
    val currentValue: String = originalValue
) : CustomFieldItemState {
    override val isModified: Boolean
        get() = originalValue.compareTo(currentValue) != 0

    override fun getValueForPatching(): Any? = currentValue.toLong()

    override fun getSavedItem(): CustomFieldItemState = copy(originalValue = currentValue)
}

data class UrlItemState(
    override val id: Long,
    override val description: String?,
    override val label: String,
    val originalValue: String,
    val currentValue: String = originalValue
) : CustomFieldItemState,
    EditableItem {
    override val isModified: Boolean
        get() = originalValue != currentValue

    override fun getValueForPatching(): Any? = currentValue

    override fun getSavedItem(): CustomFieldItemState = copy(originalValue = currentValue)
}

data class DateItemState(
    override val id: Long,
    override val description: String?,
    override val label: String,
    val originalValue: LocalDate?,
    val currentValue: LocalDate? = originalValue
) : CustomFieldItemState {
    override val isModified: Boolean
        get() {
            if (originalValue == null && currentValue == null) {
                return false
            }
            if (originalValue == null || currentValue == null) {
                return true
            }
            return originalValue != currentValue
        }

    override fun getValueForPatching(): Any? = currentValue

    override fun getSavedItem(): CustomFieldItemState = copy(originalValue = currentValue)
}

data class DropdownItemState(
    override val id: Long,
    override val description: String?,
    override val label: String,
    val originalValue: String?,
    val currentValue: String? = originalValue,
    val options: ImmutableList<String>?
) : CustomFieldItemState {
    override val isModified: Boolean
        get() = originalValue != currentValue

    override fun getValueForPatching(): Any? = currentValue

    override fun getSavedItem(): CustomFieldItemState = copy(originalValue = currentValue)
}

data class CheckboxItemState(
    override val id: Long,
    override val description: String?,
    override val label: String,
    val originalValue: Boolean,
    val currentValue: Boolean = originalValue
) : CustomFieldItemState {
    override val isModified: Boolean
        get() = originalValue != currentValue

    override fun getValueForPatching(): Any? = currentValue

    override fun getSavedItem(): CustomFieldItemState = copy(originalValue = currentValue)
}
