package com.grappim.taigamobile.uikit.widgets.text

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.grappim.taigamobile.strings.RString

/**
 * Common nothing to see here text
 */
@Composable
fun NothingToSeeHereText() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(RString.nothing_to_see),
            color = MaterialTheme.colorScheme.outline
        )
    }
}
