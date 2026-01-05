package com.grappim.taigamobile.feature.workitem.ui.widgets.badge

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.utils.ui.textColor

@Composable
fun WorkItemClickableBadgeWidget(
    title: String,
    color: Color,
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    isClickable: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            )
        )
    )

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = color
    ) {
        Row(
            modifier = Modifier
                .then(
                    if (isClickable) {
                        Modifier.clickable(
                            indication = ripple(),
                            onClick = onClick,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.widthIn(max = 120.dp),
                text = title,
                color = color.textColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (isClickable) {
                Image(
                    painter = painterResource(RDrawable.ic_arrow_down),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color.textColor()),
                    modifier = Modifier.rotate(if (isLoading) rotation else 0f)
                )
            } else {
                Spacer(Modifier.width(6.dp))
            }
        }
    }
}
