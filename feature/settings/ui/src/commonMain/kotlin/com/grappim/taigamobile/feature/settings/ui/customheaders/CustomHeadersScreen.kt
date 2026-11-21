package com.grappim.taigamobile.feature.settings.ui.customheaders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.kit.customheaders.CustomHeader
import com.grappim.kit.customheaders.CustomHeaderError
import com.grappim.kit.uikit.NativeText
import com.grappim.kit.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.kit.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.kit.uikit.widgets.topbar.TopBarActionTextButton
import com.grappim.kit.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.custom_headers_add
import com.grappim.taigamobile.strings.generated.resources.custom_headers_description
import com.grappim.taigamobile.strings.generated.resources.custom_headers_error_duplicate_name
import com.grappim.taigamobile.strings.generated.resources.custom_headers_error_invalid_name
import com.grappim.taigamobile.strings.generated.resources.custom_headers_error_invalid_value
import com.grappim.taigamobile.strings.generated.resources.custom_headers_error_missing_name
import com.grappim.taigamobile.strings.generated.resources.custom_headers_error_reserved_name
import com.grappim.taigamobile.strings.generated.resources.custom_headers_name_label
import com.grappim.taigamobile.strings.generated.resources.custom_headers_name_placeholder
import com.grappim.taigamobile.strings.generated.resources.custom_headers_remove
import com.grappim.taigamobile.strings.generated.resources.custom_headers_value_label
import com.grappim.taigamobile.strings.generated.resources.save
import com.grappim.taigamobile.strings.generated.resources.settings_custom_headers
import com.grappim.taigamobile.uikit.state.LocalOfflineState
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CustomHeadersScreen(showSnackbar: (NativeText) -> Unit, viewModel: CustomHeadersViewModel = koinViewModel()) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isOffline = LocalOfflineState.current

    LaunchedEffect(state.isSaveEnabled, isOffline) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.settings_custom_headers),
                navigationIcon = NavigationIconConfig.Back(),
                actions = persistentListOf(
                    TopBarActionTextButton(
                        text = NativeText.Resource(RString.save),
                        enabled = state.isSaveEnabled && !isOffline,
                        onClick = state.onSaveClick
                    )
                )
            )
        )
    }

    ObserveAsEvents(viewModel.snackBarMessage) { showSnackbar(it) }

    CustomHeadersScreenContent(state = state)
}

@Composable
private fun CustomHeadersScreenContent(state: CustomHeadersState) {
    val enabled = !state.isSaving

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = mainHorizontalScreenPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(RString.custom_headers_description),
                style = MaterialTheme.typography.bodyMedium
            )

            state.rows.forEachIndexed { index, row ->
                CustomHeaderRowFields(
                    row = row,
                    enabled = enabled,
                    onNameChange = { state.onNameChange(index, it) },
                    onValueChange = { state.onValueChange(index, it) },
                    onRemove = { state.onRemove(index) }
                )
            }

            TextButton(onClick = state.onAdd, enabled = enabled) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                Text(stringResource(RString.custom_headers_add))
            }
        }
    }
}

@Composable
private fun CustomHeaderRowFields(
    row: CustomHeaderRow,
    enabled: Boolean,
    onNameChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    val error = row.error
    val isValueError = error == CustomHeaderError.InvalidValue
    val nameError = error?.takeUnless { isValueError }

    Row(verticalAlignment = Alignment.Top) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = row.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(RString.custom_headers_name_label)) },
                placeholder = { Text(stringResource(RString.custom_headers_name_placeholder)) },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(stringResource(it.message())) } },
                singleLine = true,
                enabled = enabled
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = row.value,
                onValueChange = onValueChange,
                label = { Text(stringResource(RString.custom_headers_value_label)) },
                isError = isValueError,
                supportingText = error?.takeIf { isValueError }?.let { { Text(stringResource(it.message())) } },
                singleLine = true,
                enabled = enabled
            )
        }
        IconButton(onClick = onRemove, enabled = enabled) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(RString.custom_headers_remove)
            )
        }
    }
}

private fun CustomHeaderError.message(): StringResource = when (this) {
    CustomHeaderError.MissingName -> RString.custom_headers_error_missing_name
    CustomHeaderError.InvalidName -> RString.custom_headers_error_invalid_name
    CustomHeaderError.ReservedName -> RString.custom_headers_error_reserved_name
    CustomHeaderError.DuplicateName -> RString.custom_headers_error_duplicate_name
    CustomHeaderError.InvalidValue -> RString.custom_headers_error_invalid_value
}

@PreviewTaigaDarkLight
@Composable
private fun CustomHeadersScreenContentEmptyPreview() {
    TaigaMobilePreviewTheme {
        CustomHeadersScreenContent(state = CustomHeadersState())
    }
}

@PreviewTaigaDarkLight
@Composable
private fun CustomHeadersScreenContentPreview() {
    TaigaMobilePreviewTheme {
        CustomHeadersScreenContent(
            state = CustomHeadersState(
                rows = persistentListOf(
                    CustomHeaderRow(name = "CF-Access-Client-Id", value = "abc.access"),
                    CustomHeaderRow(name = "Host", value = "taiga.lan", error = CustomHeaderError.ReservedName)
                ),
                saved = persistentListOf(CustomHeader(name = "CF-Access-Client-Id", value = "abc.access"))
            )
        )
    }
}
