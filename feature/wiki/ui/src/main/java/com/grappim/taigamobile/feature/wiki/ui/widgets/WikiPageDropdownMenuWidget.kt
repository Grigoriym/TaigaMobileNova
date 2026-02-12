package com.grappim.taigamobile.feature.wiki.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.grappim.taigamobile.feature.wiki.ui.page.details.WikiPageState
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.dialogTonalElevation

@Composable
fun WikiPageDropDownMenuWidget(state: WikiPageState, isOffline: Boolean, modifier: Modifier = Modifier) {
    if (state.canModifyPage) {
        Box(
            modifier = modifier
        ) {
            DropdownMenu(
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.surfaceColorAtElevation(dialogTonalElevation)
                ),
                expanded = state.isDropdownMenuExpanded,
                onDismissRequest = { state.setDropdownMenuExpanded(false) }
            ) {
                DropdownMenuItem(
                    enabled = !isOffline,
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
}
