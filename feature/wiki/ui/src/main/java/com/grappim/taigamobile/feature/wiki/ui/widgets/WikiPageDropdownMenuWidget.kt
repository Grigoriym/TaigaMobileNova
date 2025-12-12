package com.grappim.taigamobile.feature.wiki.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.grappim.taigamobile.feature.wiki.ui.page.WikiPageState
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.dialogTonalElevation
import com.grappim.taigamobile.utils.ui.surfaceColorAtElevationInternal

@Composable
fun WikiPageDropDownMenuWidget(state: WikiPageState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
    ) {
        DropdownMenu(
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surfaceColorAtElevationInternal(dialogTonalElevation)
            ),
            expanded = state.isDropdownMenuExpanded,
            onDismissRequest = { state.setDropdownMenuExpanded(false) }
        ) {
            DropdownMenuItem(
                onClick = {
                    state.setDropdownMenuExpanded(false)
                    state.setDeleteAlertVisible(true)
                },
                text = {
                    Text(
                        text = stringResource(RString.delete),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )
        }
    }
}
