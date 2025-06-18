package io.eugenethedev.taigamobile.wiki.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.grappim.taigamobile.R
import io.eugenethedev.taigamobile.ui.theme.dialogTonalElevation
import io.eugenethedev.taigamobile.ui.utils.surfaceColorAtElevationInternal

@Composable
fun WikiPageDropDownMenu(
    modifier: Modifier = Modifier,
    state: WikiPageState,
) {
    Box(
        modifier = modifier,
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
                    state.setEditPageVisible(true)
                },
                text = {
                    Text(
                        text = stringResource(R.string.edit),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )
            DropdownMenuItem(
                onClick = {
                    state.setDropdownMenuExpanded(false)
                    state.setDeleteAlertVisible(true)
                },
                text = {
                    Text(
                        text = stringResource(R.string.delete),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )
        }
    }
}
