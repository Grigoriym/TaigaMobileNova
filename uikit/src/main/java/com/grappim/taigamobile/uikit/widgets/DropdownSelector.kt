package com.grappim.taigamobile.uikit.widgets

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.grappim.taigamobile.uikit.theme.dialogTonalElevation
import com.grappim.taigamobile.uikit.utils.RDrawable

/**
 * Dropdown selector with animated arrow
 */
@Composable
fun <T> DropdownSelector(
    items: List<T>,
    selectedItem: T,
    onItemSelect: (T) -> Unit,
    itemContent: @Composable (T) -> Unit,
    selectedItemContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    takeMaxWidth: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    tint: Color = MaterialTheme.colorScheme.primary,
    onExpand: () -> Unit = {},
    onDismissRequest: () -> Unit = {},
    canModify: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(false) }

    val transitionState = remember { MutableTransitionState(isExpanded) }
    transitionState.targetState = isExpanded

    if (isExpanded) onExpand()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .let { if (takeMaxWidth) it.fillMaxWidth() else it }
                .then(
                    if (canModify) {
                        Modifier.clickable {
                            isExpanded = !isExpanded
                        }
                    } else {
                        Modifier
                    }
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = horizontalArrangement
        ) {
            selectedItemContent(selectedItem)

            val arrowRotation by rememberTransition(
                transitionState,
                "arrow"
            ).animateFloat { if (it) -180f else 0f }

            Icon(
                painter = painterResource(RDrawable.ic_arrow_down),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.rotate(arrowRotation)
            )
        }

        DropdownMenu(
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surfaceColorAtElevation(dialogTonalElevation)
            ),
            expanded = isExpanded,
            onDismissRequest = {
                isExpanded = false
                onDismissRequest()
            }
        ) {
            items.forEach {
                DropdownMenuItem(
                    onClick = {
                        isExpanded = false
                        onItemSelect(it)
                    },
                    text = {
                        itemContent(it)
                    }
                )
            }
        }
    }
}
