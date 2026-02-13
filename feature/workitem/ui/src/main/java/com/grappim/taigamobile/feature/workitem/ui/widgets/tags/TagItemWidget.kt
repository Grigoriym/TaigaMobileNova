package com.grappim.taigamobile.feature.workitem.ui.widgets.tags

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.feature.workitem.ui.models.SelectableTagUI
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.Chip
import com.grappim.taigamobile.utils.ui.textColor

@Composable
fun TagItemWidget(
    isOffline: Boolean,
    tag: SelectableTagUI,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
    canModify: Boolean = true
) {
    val textColor = tag.color.textColor()

    Chip(
        modifier = modifier.padding(end = 4.dp, bottom = 4.dp),
        color = tag.color
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = tag.name,
                color = textColor
            )

            if (canModify) {
                Spacer(Modifier.width(2.dp))

                IconButton(
                    enabled = !isOffline,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape),
                    onClick = onRemoveClick
                ) {
                    Icon(
                        painter = painterResource(RDrawable.ic_remove),
                        contentDescription = null,
                        tint = textColor
                    )
                }
            }
        }
    }
}
