@file:OptIn(ExperimentalMaterial3Api::class)

package com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.grappim.taigamobile.feature.projects.domain.ProjectValueItem
import com.grappim.taigamobile.feature.projects.domain.ProjectValueType
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.cancel
import com.grappim.taigamobile.strings.generated.resources.custom_color
import com.grappim.taigamobile.strings.generated.resources.delete
import com.grappim.taigamobile.strings.generated.resources.project_values_add_title
import com.grappim.taigamobile.strings.generated.resources.project_values_by_default
import com.grappim.taigamobile.strings.generated.resources.project_values_color
import com.grappim.taigamobile.strings.generated.resources.project_values_days_to_due_hint
import com.grappim.taigamobile.strings.generated.resources.project_values_delete_text
import com.grappim.taigamobile.strings.generated.resources.project_values_edit_title
import com.grappim.taigamobile.strings.generated.resources.project_values_is_archived
import com.grappim.taigamobile.strings.generated.resources.project_values_is_closed
import com.grappim.taigamobile.strings.generated.resources.project_values_move_to
import com.grappim.taigamobile.strings.generated.resources.project_values_name_hint
import com.grappim.taigamobile.strings.generated.resources.project_values_value_hint
import com.grappim.taigamobile.strings.generated.resources.save
import com.grappim.taigamobile.uikit.generated.resources.ic_add
import com.grappim.taigamobile.uikit.generated.resources.ic_delete
import com.grappim.taigamobile.uikit.generated.resources.ic_edit
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.ErrorStateWidget
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.uikit.widgets.dialog.TaigaLoadingDialog
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarController
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import com.grappim.taigamobile.utils.ui.StaticStringColor
import com.grappim.taigamobile.utils.ui.asColor
import com.grappim.taigamobile.utils.ui.toColor
import com.grappim.taigamobile.utils.ui.toHex
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProjectValuesScreen(showSnackbar: (NativeText) -> Unit, viewModel: ProjectValuesViewModel = koinViewModel()) {
    val topBarController: TopBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    val titleRes = state.type.toTitleRes()

    LaunchedEffect(titleRes) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(titleRes),
                navigationIcon = NavigationIconConfig.Back(),
                actions = persistentListOf(
                    TopBarActionIconButton(
                        drawable = RDrawable.ic_add,
                        contentDescription = "Add value",
                        onClick = state.onAddClick
                    )
                )
            )
        )
    }

    ObserveAsEvents(viewModel.snackBarMessage) { message ->
        showSnackbar(message)
    }

    TaigaLoadingDialog(isVisible = state.isOperationLoading)

    if (state.isEditDialogVisible) {
        EditProjectValueDialog(state = state)
    }

    if (state.isDeleteDialogVisible) {
        DeleteProjectValueDialog(state = state)
    }

    ProjectValuesContent(state = state)
}

@Composable
private fun ProjectValuesContent(state: ProjectValuesState) {
    if (state.error.isNotEmpty() && state.items.isEmpty()) {
        ErrorStateWidget(
            message = state.error,
            onRetry = state.refresh
        )
    } else {
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            onRefresh = state.refresh,
            isRefreshing = state.isLoading
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.items, key = { it.id }) { item ->
                    ProjectValueItemRow(item = item, type = state.type, state = state)
                }
            }
        }
    }
}

@Composable
private fun ProjectValueItemRow(item: ProjectValueItem, type: ProjectValueType, state: ProjectValuesState) {
    ListItem(
        leadingContent = if (type.hasColor) {
            {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(StaticStringColor(item.color).asColor())
                )
            }
        } else {
            null
        },
        headlineContent = { Text(item.name) },
        supportingContent = { ProjectValueItemSupportingContent(item = item, type = type) },
        trailingContent = {
            Row {
                IconButton(onClick = { state.onEditClick(item) }) {
                    Icon(
                        painter = painterResource(RDrawable.ic_edit),
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                if (!item.byDefault) {
                    IconButton(onClick = { state.onDeleteClick(item) }) {
                        Icon(
                            painter = painterResource(RDrawable.ic_delete),
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun ProjectValueItemSupportingContent(item: ProjectValueItem, type: ProjectValueType) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        ProjectValueItemStatusChips(item = item, type = type)
        ProjectValueItemExtraValues(item = item, type = type)
    }
}

@Composable
private fun ProjectValueItemStatusChips(item: ProjectValueItem, type: ProjectValueType) {
    if (item.byDefault) {
        AssistChip(
            onClick = {},
            label = { Text(stringResource(RString.project_values_by_default)) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        )
    }
    if (type.hasClosed && item.isClosed) {
        AssistChip(
            onClick = {},
            label = { Text(stringResource(RString.project_values_is_closed)) }
        )
    }
    if (type.hasArchived && item.isArchived) {
        AssistChip(
            onClick = {},
            label = { Text(stringResource(RString.project_values_is_archived)) }
        )
    }
}

@Composable
private fun ProjectValueItemExtraValues(item: ProjectValueItem, type: ProjectValueType) {
    if (type.hasValue && item.value != null) {
        Text(
            text = item.value.toString(),
            style = MaterialTheme.typography.bodySmall
        )
    }
    if (type.hasDaysToDue && item.daysToDue != null) {
        Text(
            text = item.daysToDue.toString(),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private data class EditFormState(
    val name: String,
    val color: Color,
    val isClosed: Boolean,
    val isArchived: Boolean,
    val value: String,
    val daysToDue: String
)

private fun initialEditState(item: ProjectValueItem?, defaultColor: String): EditFormState = EditFormState(
    name = item?.name ?: "",
    color = (item?.color ?: defaultColor).toColor(),
    isClosed = item?.isClosed ?: false,
    isArchived = item?.isArchived ?: false,
    value = item?.value?.toString() ?: "",
    daysToDue = item?.daysToDue?.toString() ?: ""
)

@Composable
private fun EditProjectValueDialog(state: ProjectValuesState) {
    val type = state.type
    val item = state.editingItem
    var form by remember(item) {
        mutableStateOf(initialEditState(item, state.presetColors.firstOrNull() ?: "#A9AABC"))
    }

    Dialog(onDismissRequest = state.onDismissEditDialog) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (item == null) {
                        stringResource(RString.project_values_add_title)
                    } else {
                        stringResource(RString.project_values_edit_title)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TaigaHeightSpacer(16.dp)

                OutlinedTextField(
                    value = form.name,
                    onValueChange = { form = form.copy(name = it) },
                    label = { Text(stringResource(RString.project_values_name_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (type.hasColor) {
                    TaigaHeightSpacer(12.dp)
                    ColorPickerSection(
                        selectedColor = form.color,
                        presetColors = state.presetColors,
                        onColorSelect = { form = form.copy(color = it) }
                    )
                }

                if (type.hasClosed) {
                    TaigaHeightSpacer(8.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(RString.project_values_is_closed),
                            modifier = Modifier.weight(1f)
                        )
                        Switch(checked = form.isClosed, onCheckedChange = { form = form.copy(isClosed = it) })
                    }
                }

                if (type.hasArchived) {
                    TaigaHeightSpacer(8.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(RString.project_values_is_archived),
                            modifier = Modifier.weight(1f)
                        )
                        Switch(checked = form.isArchived, onCheckedChange = { form = form.copy(isArchived = it) })
                    }
                }

                if (type.hasValue) {
                    TaigaHeightSpacer(12.dp)
                    OutlinedTextField(
                        value = form.value,
                        onValueChange = { form = form.copy(value = it) },
                        label = { Text(stringResource(RString.project_values_value_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                if (type.hasDaysToDue) {
                    TaigaHeightSpacer(12.dp)
                    OutlinedTextField(
                        value = form.daysToDue,
                        onValueChange = { form = form.copy(daysToDue = it) },
                        label = { Text(stringResource(RString.project_values_days_to_due_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                TaigaHeightSpacer(16.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = state.onDismissEditDialog) {
                        Text(stringResource(RString.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            state.onSaveItem(
                                form.name,
                                form.color.toHex(),
                                form.isClosed,
                                form.isArchived,
                                form.value,
                                form.daysToDue
                            )
                        },
                        enabled = form.name.isNotBlank()
                    ) {
                        Text(stringResource(RString.save))
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorPickerSection(
    selectedColor: Color,
    presetColors: ImmutableList<String>,
    onColorSelect: (Color) -> Unit
) {
    var isCustomColorExpanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = stringResource(RString.project_values_color),
            style = MaterialTheme.typography.labelMedium
        )
        TaigaHeightSpacer(8.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(selectedColor)
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = selectedColor.toHex(),
                style = MaterialTheme.typography.labelSmall
            )
        }

        TaigaHeightSpacer(8.dp)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            presetColors.forEach { preset ->
                val presetColor = StaticStringColor(preset).asColor()
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(presetColor)
                        .border(
                            width = if (selectedColor == presetColor) 3.dp else 1.dp,
                            color = if (selectedColor == presetColor) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            shape = CircleShape
                        )
                        .clickable { onColorSelect(presetColor) }
                )
            }
        }

        TaigaHeightSpacer(8.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isCustomColorExpanded = !isCustomColorExpanded }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isCustomColorExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(RString.custom_color),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (isCustomColorExpanded) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val controller = rememberColorPickerController()
                    HsvColorPicker(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        controller = controller,
                        onColorChanged = { colorEnvelope ->
                            onColorSelect(colorEnvelope.color)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteProjectValueDialog(state: ProjectValuesState) {
    val type = state.type
    val deletingItem = state.deletingItem ?: return
    val otherItems = state.items.filter { it.id != deletingItem.id }

    if (type.deleteRequiresMoveTo) {
        var expanded by remember { mutableStateOf(false) }
        var selectedMoveTo by remember { mutableStateOf(otherItems.firstOrNull()) }

        AlertDialog(
            onDismissRequest = state.onDismissDeleteDialog,
            title = { Text(stringResource(RString.delete)) },
            text = {
                Column {
                    Text(stringResource(RString.project_values_delete_text))
                    TaigaHeightSpacer(12.dp)
                    if (otherItems.isNotEmpty()) {
                        Text(
                            text = stringResource(RString.project_values_move_to),
                            style = MaterialTheme.typography.labelMedium
                        )
                        TaigaHeightSpacer(4.dp)
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedMoveTo?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                otherItems.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item.name) },
                                        onClick = {
                                            selectedMoveTo = item
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { state.onConfirmDelete(selectedMoveTo?.id) },
                    enabled = otherItems.isEmpty() || selectedMoveTo != null
                ) {
                    Text(stringResource(RString.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = state.onDismissDeleteDialog) {
                    Text(stringResource(RString.cancel))
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = state.onDismissDeleteDialog,
            title = { Text(stringResource(RString.delete)) },
            text = { Text("Delete \"${deletingItem.name}\"?") },
            confirmButton = {
                Button(onClick = { state.onConfirmDelete(null) }) {
                    Text(stringResource(RString.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = state.onDismissDeleteDialog) {
                    Text(stringResource(RString.cancel))
                }
            }
        )
    }
}

@PreviewTaigaDarkLight
@Composable
private fun ProjectValuesScreenPreview() {
    TaigaMobilePreviewTheme {
        Surface {
            ProjectValuesContent(
                state = ProjectValuesState(
                    type = ProjectValueType.EPIC_STATUSES,
                    items = persistentListOf(
                        ProjectValueItem(id = 1, name = "New", color = "#70BD0C", order = 1, project = 1),
                        ProjectValueItem(
                            id = 2,
                            name = "In progress",
                            color = "#E44057",
                            order = 2,
                            project = 1,
                            isClosed = false
                        ),
                        ProjectValueItem(
                            id = 3,
                            name = "Done",
                            color = "#A9AABC",
                            order = 3,
                            project = 1,
                            isClosed = true
                        )
                    )
                )
            )
        }
    }
}
