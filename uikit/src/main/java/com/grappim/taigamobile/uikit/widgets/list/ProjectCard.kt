package com.grappim.taigamobile.uikit.widgets.list

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.grappim.taigamobile.core.domain.ProjectDTO
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.RDrawable

@Composable
fun ProjectCard(projectDTO: ProjectDTO, isCurrent: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = mainHorizontalScreenPadding, vertical = 4.dp),
        shape = MaterialTheme.shapes.small,
        border = if (isCurrent) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(),
                    onClick = onClick
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    modifier = Modifier.size(46.dp),
                    contentDescription = null,
                    placeholder = painterResource(RDrawable.default_avatar),
                    error = painterResource(RDrawable.default_avatar),
                    model = projectDTO.avatarUrl
                )

                Spacer(Modifier.width(8.dp))

                Column {
                    Text(
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        text = stringResource(
                            when {
                                projectDTO.isOwner -> RString.project_owner
                                projectDTO.isAdmin -> RString.project_admin
                                else -> RString.project_member
                            }
                        )
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = projectDTO.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            projectDTO.description?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(8.dp))

            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.outline
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconSize = 18.dp
                    val indicatorsSpacing = 8.dp

                    @Composable
                    fun Indicator(@DrawableRes icon: Int, value: Int) {
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = null,
                            modifier = Modifier.size(iconSize)
                        )

                        Spacer(Modifier.width(4.dp))

                        Text(
                            text = value.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Indicator(RDrawable.ic_favorite, projectDTO.fansCount)
                    Spacer(Modifier.width(indicatorsSpacing))
                    Indicator(RDrawable.ic_watch, projectDTO.watchersCount)
                    Spacer(Modifier.width(indicatorsSpacing))
                    Indicator(RDrawable.ic_team, projectDTO.members.size)

                    if (projectDTO.isPrivate) {
                        Spacer(Modifier.width(indicatorsSpacing))
                        Icon(
                            painter = painterResource(RDrawable.ic_key),
                            contentDescription = null,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
            }
        }
    }
}
