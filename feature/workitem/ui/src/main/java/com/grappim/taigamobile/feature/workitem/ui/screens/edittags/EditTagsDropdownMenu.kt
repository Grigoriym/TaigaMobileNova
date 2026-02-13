package com.grappim.taigamobile.feature.workitem.ui.screens.edittags

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.dialogTonalElevation

@Composable
internal fun EditTagsDropdownMenu(state: EditTagsState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopEnd)
    ) {
        DropdownMenu(
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surfaceColorAtElevation(
                    dialogTonalElevation
                )
            ),
            expanded = state.isDropdownMenuExpanded,
            onDismissRequest = {
                state.setDropdownMenuExpanded(false)
            }
        ) {
            DropdownMenuItem(
                onClick = state.onSaveTagDropdownClick,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "")
                },
                text = {
                    Text(text = stringResource(RString.save))
                }
            )

            DropdownMenuItem(
                onClick = state.onAddTagDropdownClick,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "")
                },
                text = {
                    Text(text = stringResource(RString.add_tag))
                }
            )
        }
    }
}
