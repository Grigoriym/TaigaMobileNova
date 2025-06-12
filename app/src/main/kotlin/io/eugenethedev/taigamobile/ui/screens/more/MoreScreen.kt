package io.eugenethedev.taigamobile.ui.screens.more

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
import androidx.navigation.NavController
import com.grappim.taigamobile.R
import io.eugenethedev.taigamobile.core.nav.Routes
import io.eugenethedev.taigamobile.ui.components.appbars.AppBarWithBackButton
import io.eugenethedev.taigamobile.ui.components.containers.ContainerBox

@Composable
fun MoreScreen(
    navController: NavController
) = Column(Modifier.fillMaxSize()) {
    AppBarWithBackButton(
        title = { Text(stringResource(R.string.more)) }
    )

    @Composable
    fun Item(
        @DrawableRes iconId: Int,
        @StringRes nameId: Int,
        route: String
    ) = ContainerBox(onClick = { navController.navigate(route) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(iconId),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.outline
            )

            Spacer(Modifier.width(8.dp))

            Text(stringResource(nameId))
        }
    }

    val space = 2.dp

    Item(R.drawable.ic_team, R.string.team, Routes.team)
    Spacer(Modifier.height(space))
    Item(R.drawable.ic_kanban, R.string.kanban, Routes.kanban)
    Spacer(Modifier.height(space))
    Item(R.drawable.ic_wiki, R.string.wiki, Routes.wiki_selector)
    Spacer(Modifier.height(space))
    Item(R.drawable.ic_settings, R.string.settings, Routes.settings)
}
