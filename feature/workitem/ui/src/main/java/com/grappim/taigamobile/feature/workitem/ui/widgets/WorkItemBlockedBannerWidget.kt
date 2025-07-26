package com.grappim.taigamobile.feature.workitem.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.taigaRed
import com.grappim.taigamobile.uikit.utils.RDrawable
import kotlin.text.isNotEmpty

@Composable
fun WorkItemBlockedBannerWidget(blockedNote: String?, modifier: Modifier = Modifier) {
    if (blockedNote != null) {
        Column(modifier = modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(taigaRed, MaterialTheme.shapes.medium)
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(RDrawable.ic_lock),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(stringResource(RString.blocked))
                }

                if (blockedNote.isNotEmpty()) {
                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = blockedNote,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}
