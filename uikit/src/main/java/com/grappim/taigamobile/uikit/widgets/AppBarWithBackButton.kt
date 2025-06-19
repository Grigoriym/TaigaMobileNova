package com.grappim.taigamobile.uikit.widgets

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.uikit.utils.RDrawable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Deprecated("remove it since we have a global topBar")
fun AppBarWithBackButton(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    navigateBack: (() -> Unit)? = null
) {
    TopAppBar(
        modifier = modifier,
        title = title,
        navigationIcon = navigateBack?.let {
            {
                IconButton(onClick = it) {
                    Icon(
                        painter = painterResource(RDrawable.ic_arrow_back),
                        contentDescription = "Back button",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        } ?: {},
        actions = actions
    )
}
