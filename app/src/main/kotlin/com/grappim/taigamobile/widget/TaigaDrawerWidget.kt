package com.grappim.taigamobile.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@Composable
fun TaigaDrawerWidget(
    screens: List<DrawerDestination>,
    currentItem: DrawerDestination?,
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
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Taiga Nova",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    LazyColumn {
                        items(screens) { item ->
                            NavigationDrawerItem(
                                label = { Text(stringResource(item.label)) },
                                selected = currentItem == item,
                                icon = {
                                    Icon(
                                        painter = painterResource(item.icon),
                                        contentDescription = stringResource(item.label)
                                    )
                                },
                                onClick = {
                                    onDrawerItemClick(item)
                                }
                            )
                        }
                    }
                }
            }
        },
        content = content
    )
}
