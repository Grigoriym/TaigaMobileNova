package io.eugenethedev.taigamobile.ui.components.appbars

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.eugenethedev.taigamobile.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarWithBackButton(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    navigateBack: (() -> Unit)? = null
) = TopAppBar(
    title = title,
    navigationIcon = navigateBack?.let {
        {
            IconButton(onClick = it) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    } ?: {},
    actions = actions,
    modifier = modifier.statusBarsPadding()
)
