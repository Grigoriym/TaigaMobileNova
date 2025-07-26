package com.grappim.taigamobile.feature.workitem.ui.widgets.customfields

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.DatePickerDialogWidget
import com.grappim.taigamobile.uikit.widgets.DropdownSelector
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.uikit.widgets.TaigaWidthSpacer
import com.grappim.taigamobile.uikit.widgets.loader.DotsLoader
import com.grappim.taigamobile.uikit.widgets.text.MarkdownTextWidget
import com.grappim.taigamobile.uikit.widgets.text.SectionTitleExpandable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import timber.log.Timber
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun LazyListScope.customFieldsSectionWidget(
    customFieldStateItems: ImmutableList<CustomFieldItemState>,
    isCustomFieldsLoading: Boolean,
    isCustomFieldsWidgetExpanded: Boolean,
    setIsCustomFieldsWidgetExpanded: (Boolean) -> Unit,
    onCustomFieldChange: (CustomFieldItemState) -> Unit,
    onCustomFieldSave: (CustomFieldItemState) -> Unit,
    onCustomFieldEditToggle: (CustomFieldItemState) -> Unit,
    editingItemIds: ImmutableSet<Long>
) {
    if (customFieldStateItems.isNotEmpty()) {
        item {
            SectionTitleExpandable(
                text = stringResource(RString.custom_fields),
                isExpanded = isCustomFieldsWidgetExpanded,
                onExpandClick = {
                    setIsCustomFieldsWidgetExpanded(!isCustomFieldsWidgetExpanded)
                }
            )
        }

        if (isCustomFieldsWidgetExpanded) {
            itemsIndexed(
                items = customFieldStateItems,
                key = { index, item -> item.id },
                contentType = { index, item -> item }
            ) { index, item ->
                CustomFieldWidget(
                    editingItemIds = editingItemIds,
                    item = item,
                    onItemChange = onCustomFieldChange,
                    onItemSave = onCustomFieldSave,
                    onItemEdit = onCustomFieldEditToggle
                )
                if (index < customFieldStateItems.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                        thickness = DividerDefaults.Thickness,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (isCustomFieldsLoading) {
                item {
                    TaigaHeightSpacer(8.dp)
                }
                item {
                    DotsLoader()
                }
            }

            item {
                TaigaHeightSpacer(10.dp)
            }
        }
    }
}

@Composable
private fun CustomFieldWidget(
    editingItemIds: ImmutableSet<Long>,
    item: CustomFieldItemState,
    onItemChange: (CustomFieldItemState) -> Unit,
    onItemSave: (CustomFieldItemState) -> Unit,
    onItemEdit: (CustomFieldItemState) -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditableItem = item is EditableItem
    val isEditMode = isEditableItem && item.id in editingItemIds

//    val fieldState by remember { mutableStateOf(FieldState.Default) }
    val indicationColor = if (item.isModified) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
//    val borderColor = when (fieldState) {
//        FieldState.Focused -> MaterialTheme.colorScheme.primary
//        FieldState.Error -> MaterialTheme.colorScheme.error
//        FieldState.Default -> indicationColor
//    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = item.label,
            style = MaterialTheme.typography.titleMedium
        )

        if (item.description != null) {
            Text(
                text = requireNotNull(item.description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        TaigaHeightSpacer(4.dp)

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
//                    .border(
//                        width = 1.dp,
//                        color = borderColor,
//                        //                        color = if (customField.type == CustomFieldType.Checkbox) {
//                        //                            Color.Transparent
//                        //                        } else {
//                        //                            borderColor
//                        //                        },
//                        shape = MaterialTheme.shapes.small
//                    )
//                    .clip(MaterialTheme.shapes.extraSmall)
                    .padding(6.dp)
            ) {
                when (item) {
                    is TextItemState -> CustomFieldTextItemWidget(
                        item = item,
                        onItemChange = onItemChange
                    )

                    is MultilineTextItemState -> CustomFieldMultilineItemWidget(
                        item = item,
                        onItemChange = onItemChange
                    )

                    is RichTextItemState -> CustomFieldRichTextItemWidget(
                        item = item,
                        onItemChange = onItemChange,
                        isEditMode = isEditMode
                    )

                    is NumberItemState -> CustomFieldNumberItemWidget(
                        item = item,
                        onItemChange = onItemChange
                    )

                    is UrlItemState -> {
                        CustomFieldUrlItemWidget(
                            item = item,
                            onItemChange = onItemChange,
                            isEditMode = isEditMode
                        )
                    }

                    is DateItemState -> {
                        CustomFieldDateItemWidget(
                            item = item,
                            onItemChange = onItemChange
                        )
                    }

                    is CheckboxItemState -> {
                        CustomFieldCheckboxWidget(
                            item = item,
                            onItemChange = onItemChange
                        )
                    }

                    is DropdownItemState -> {
                        CustomFieldDropdownItemWidget(
                            item = item,
                            onItemChange = onItemChange
                        )
                    }
                }
            }

//            Row {
            if (isEditableItem) {
                TaigaWidthSpacer(4.dp)

                IconButton(
                    onClick = {
                        onItemEdit(item)
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        painter = if (isEditMode) {
                            painterResource(RDrawable.ic_undo)
                        } else {
                            painterResource(RDrawable.ic_edit)
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                TaigaWidthSpacer(4.dp)
            }

            IconButton(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                enabled = item.isModified,
                onClick = {
                    onItemSave(item)
//                    if (fieldState != FieldState.Error && value != customField.value) {
//                        focusManager.clearFocus()
//                        onSaveClick()
//                    }
                }
            ) {
                Icon(
                    painter = painterResource(RDrawable.ic_save),
                    contentDescription = null,
                    tint = indicationColor
                )
            }
//            }
        }
    }
}

@Composable
private fun CustomFieldTextItemWidget(
    item: TextItemState,
    onItemChange: (TextItemState) -> Unit,
    modifier: Modifier = Modifier
) {
    CustomFieldTextWidget(
        modifier = modifier,
        value = item.currentValue,
        onItemChange = { newValue ->
            onItemChange(item.copy(currentValue = newValue))
        },
        placeholder = stringResource(RString.custom_field_text),
        singleLine = true
    )
}

@Composable
private fun CustomFieldMultilineItemWidget(
    item: MultilineTextItemState,
    onItemChange: (MultilineTextItemState) -> Unit,
    modifier: Modifier = Modifier
) {
    CustomFieldTextWidget(
        modifier = modifier,
        value = item.currentValue,
        onItemChange = { newValue ->
            onItemChange(item.copy(currentValue = newValue))
        },
        placeholder = stringResource(RString.custom_field_multiline),
        singleLine = false
    )
}

@Composable
private fun CustomFieldNumberItemWidget(
    item: NumberItemState,
    onItemChange: (NumberItemState) -> Unit,
    modifier: Modifier = Modifier
) {
    CustomFieldTextWidget(
        modifier = modifier,
        value = item.currentValue,
        onItemChange = { newValue ->
            onItemChange(item.copy(currentValue = newValue))
        },
        placeholder = stringResource(RString.custom_field_number),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number
        )
    )
}

@Composable
private fun CustomFieldDateItemWidget(
    item: DateItemState,
    onItemChange: (DateItemState) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    var isDatePickerVisible by remember { mutableStateOf(false) }

    DatePickerDialogWidget(
        isVisible = isDatePickerVisible,
        onDismissRequest = {
            isDatePickerVisible = false
        },
        onDismissButonClick = {
            isDatePickerVisible = false
        },
        onConfirmButtonClick = { dateMillis ->
            if (dateMillis != null) {
                onItemChange(
                    item.copy(
                        currentValue = Instant.ofEpochMilli(dateMillis)
                            .atOffset(ZoneOffset.UTC)
                            .toLocalDate()
                    )
                )
            }
            isDatePickerVisible = false
        }
    )

    CustomFieldBoxParent(
        isFocused = item.isModified,
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                isDatePickerVisible = true
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = item.currentValue?.format(dateFormatter)
                    ?: stringResource(RString.date_hint),
                style = MaterialTheme.typography.bodyLarge
            )

            if (item.currentValue != null) {
                Spacer(Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        isDatePickerVisible = false
                        onItemChange(item.copy(currentValue = null))
                    },
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        painter = painterResource(RDrawable.ic_remove),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomFieldUrlItemWidget(
    item: UrlItemState,
    onItemChange: (UrlItemState) -> Unit,
    isEditMode: Boolean,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val isEnabled = item.currentValue.isNotEmpty()

    CustomFieldTextWidget(
        modifier = modifier,
        value = item.currentValue,
        onItemChange = { newValue ->
            onItemChange(item.copy(currentValue = newValue))
        },
        placeholder = stringResource(RString.custom_field_url),
        singleLine = true,
        enabled = isEditMode,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Uri
        ),
        trailingIcon = if (!isEditMode && item.currentValue.isNotEmpty()) {
            {
                IconButton(
                    enabled = isEnabled,
                    onClick = {
                        try {
                            uriHandler.openUri(item.currentValue)
                        } catch (e: IllegalArgumentException) {
                            Timber.e(e)
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(RDrawable.ic_open),
                        contentDescription = null,
                        tint = if (isEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    )
                }
            }
        } else {
            null
        }
    )
}

@Composable
private fun CustomFieldCheckboxWidget(
    item: CheckboxItemState,
    onItemChange: (CheckboxItemState) -> Unit,
    modifier: Modifier = Modifier
) {
    CustomFieldBoxParent(
        modifier = modifier,
        isFocused = item.isModified
    ) {
        Checkbox(
            modifier = Modifier.padding(6.dp),
            checked = item.currentValue,
            onCheckedChange = {
                onItemChange(item.copy(currentValue = it))
            }
        )
    }
}

@Composable
private fun CustomFieldRichTextItemWidget(
    isEditMode: Boolean,
    item: RichTextItemState,
    onItemChange: (RichTextItemState) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isEditMode) {
        CustomFieldTextWidget(
            modifier = modifier,
            value = item.currentValue,
            onItemChange = { newValue ->
                onItemChange(item.copy(currentValue = newValue))
            },
            placeholder = stringResource(RString.custom_field_rich_text),
            singleLine = false
        )
    } else {
        CustomFieldBoxParent(
            modifier = modifier,
            isFocused = false
        ) {
            MarkdownTextWidget(
                modifier = Modifier.padding(16.dp),
                text = item.currentValue
            )
        }
    }
}

@Composable
private fun CustomFieldDropdownItemWidget(
    item: DropdownItemState,
    onItemChange: (DropdownItemState) -> Unit,
    modifier: Modifier = Modifier
) {
    val option = item.currentValue.orEmpty()

    CustomFieldBoxParent(
        modifier = modifier,
        isFocused = item.isModified
    ) {
        DropdownSelector(
            modifier = Modifier.padding(16.dp),
            items = item.options?.toList() ?: emptyList(),
            selectedItem = option,
            onItemSelect = {
                onItemChange(item.copy(currentValue = it))
            },
            itemContent = {
                if (it.isNotEmpty()) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(
                        text = stringResource(RString.empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            selectedItemContent = {
                Text(
                    text = option,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            takeMaxWidth = true,
            horizontalArrangement = Arrangement.SpaceBetween,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun CustomFieldBoxParent(
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isFocused) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                shape = MaterialTheme.shapes.small
            )
    ) {
        content()
    }
}

@Composable
private fun CustomFieldTextWidget(
    value: String,
    onItemChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = { newValue ->
            onItemChange(newValue)
        },
        singleLine = singleLine,
        placeholder = {
            Text(placeholder)
        },
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
        shape = MaterialTheme.shapes.small
    )
}
