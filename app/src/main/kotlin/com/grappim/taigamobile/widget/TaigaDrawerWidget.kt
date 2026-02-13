package com.grappim.taigamobile.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.core.nav.DrawerDestination
import com.grappim.taigamobile.core.nav.DrawerItem
import com.grappim.taigamobile.core.nav.IconSource
import com.grappim.taigamobile.strings.RString
import kotlinx.collections.immutable.ImmutableList

@Composable
fun TaigaDrawerWidget(
    drawerItems: ImmutableList<DrawerItem>,
    currentTopLevelDestination: DrawerDestination?,
    onDrawerItemClick: (DrawerDestination) -> Unit,
    drawerState: DrawerState,
    modifier: Modifier = Modifier,
    gesturesEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        modifier = modifier,
        gesturesEnabled = gesturesEnabled,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerState = drawerState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(RString.app_name),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Column {
                        drawerItems.forEach { drawerItem ->
                            when (drawerItem) {
                                is DrawerItem.Group -> {
                                    Text(
                                        text = stringResource(drawerItem.label),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(
                                            start = 16.dp,
                                            top = 16.dp,
                                            bottom = 8.dp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Column {
                                        drawerItem.items.forEach { destination ->
                                            NavigationDrawerItem(
                                                label = { Text(stringResource(destination.label)) },
                                                selected = currentTopLevelDestination == destination.destination,
                                                icon = {
                                                    when (val iconSource = destination.icon) {
                                                        is IconSource.Vector -> Icon(
                                                            imageVector = iconSource.imageVector,
                                                            contentDescription = stringResource(destination.label)
                                                        )

                                                        is IconSource.Resource -> Icon(
                                                            painter = painterResource(iconSource.resourceId),
                                                            contentDescription = stringResource(destination.label)
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    onDrawerItemClick(destination.destination)
                                                }
                                            )
                                        }
                                    }
                                }

                                is DrawerItem.Destination -> {
                                    NavigationDrawerItem(
                                        label = { Text(stringResource(drawerItem.label)) },
                                        selected = currentTopLevelDestination == drawerItem.destination,
                                        icon = {
                                            when (val iconSource = drawerItem.icon) {
                                                is IconSource.Vector -> Icon(
                                                    imageVector = iconSource.imageVector,
                                                    contentDescription = stringResource(drawerItem.label)
                                                )

                                                is IconSource.Resource -> Icon(
                                                    painter = painterResource(iconSource.resourceId),
                                                    contentDescription = stringResource(drawerItem.label)
                                                )
                                            }
                                        },
                                        onClick = {
                                            onDrawerItemClick(drawerItem.destination)
                                        }
                                    )
                                }

                                is DrawerItem.Divider -> {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        content = content
    )
}
