package com.grappim.taigamobile.uikit.widgets.text

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.uikit.utils.RDrawable

/**
 * Title with optional add button
 */
@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 0.dp,
    bottomPadding: Dp = 6.dp,
    onAddClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .padding(bottom = bottomPadding)
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(6.dp)
        )

        onAddClick?.let {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                    .clip(MaterialTheme.shapes.small)
                    .clickable(
                        onClick = it,
                        role = Role.Button,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true)
                    )
                    .padding(6.dp)
            ) {
                Icon(
                    painter = painterResource(RDrawable.ic_add),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
