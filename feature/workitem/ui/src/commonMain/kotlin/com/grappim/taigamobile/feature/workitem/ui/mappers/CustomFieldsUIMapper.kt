package com.grappim.taigamobile.feature.workitem.ui.mappers

import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFieldType
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFields
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CheckboxItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.DateItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.DropdownItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.MultilineTextItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.NumberItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.RichTextItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.TextItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.UrlItemState
import com.grappim.taigamobile.utils.formatter.decimal.DecimalFormatSimple
import com.grappim.taigamobile.utils.formatter.decimal.DecimalFormatter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.core.annotation.Factory

@Factory
class CustomFieldsUIMapper(
    @param:DecimalFormatSimple private val dfSimple: DecimalFormatter
) {
    fun toUI(customFields: CustomFields): ImmutableList<CustomFieldItemState> = customFields.fields.map { field ->
        when (field.type) {
            CustomFieldType.Text -> {
                TextItemState(
                    id = field.id,
                    label = field.name,
                    description = field.description,
                    originalValue = field.value?.stringValue ?: "",
                    currentValue = field.value?.stringValue ?: ""
                )
            }

            CustomFieldType.Multiline -> {
                MultilineTextItemState(
                    id = field.id,
                    label = field.name,
                    description = field.description,
                    originalValue = field.value?.stringValue ?: "",
                    currentValue = field.value?.stringValue ?: ""
                )
            }

            CustomFieldType.RichText -> {
                RichTextItemState(
                    id = field.id,
                    label = field.name,
                    description = field.description,
                    originalValue = field.value?.stringValue ?: "",
                    currentValue = field.value?.stringValue ?: ""
                )
            }

            CustomFieldType.Number -> {
                NumberItemState(
                    id = field.id,
                    label = field.name,
                    description = field.description,
                    originalValue = dfSimple.format(
                        field.value?.doubleValue ?: 0.0
                    ),
                    currentValue = dfSimple.format(field.value?.doubleValue ?: 0.0)
                )
            }

            CustomFieldType.Url -> {
                UrlItemState(
                    id = field.id,
                    label = field.name,
                    description = field.description,
                    originalValue = field.value?.stringValue ?: "",
                    currentValue = field.value?.stringValue ?: ""
                )
            }

            CustomFieldType.Date -> {
                DateItemState(
                    id = field.id,
                    label = field.name,
                    description = field.description,
                    originalValue = field.value?.dateValue,
                    currentValue = field.value?.dateValue
                )
            }

            CustomFieldType.Checkbox -> {
                CheckboxItemState(
                    id = field.id,
                    label = field.name,
                    description = field.description,
                    originalValue = field.value?.booleanValue ?: false,
                    currentValue = field.value?.booleanValue ?: false
                )
            }

            CustomFieldType.Dropdown -> {
                DropdownItemState(
                    id = field.id,
                    label = field.name,
                    description = field.description,
                    options = field.options?.toImmutableList(),
                    originalValue = field.value?.stringValue,
                    currentValue = field.value?.stringValue
                )
            }
        }
    }.toImmutableList()
}
