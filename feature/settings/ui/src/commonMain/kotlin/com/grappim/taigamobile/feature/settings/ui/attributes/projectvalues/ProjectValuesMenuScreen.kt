package com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.feature.projects.domain.ProjectValueType
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.settings_project_values
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProjectValuesMenuScreen(goToProjectValues: (ProjectValueType) -> Unit) {
    val topBarController = LocalTopBarConfig.current

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.settings_project_values),
                navigationIcon = NavigationIconConfig.Back()
            )
        )
    }

    ProjectValuesMenuContent(goToProjectValues = goToProjectValues)
}

@Composable
private fun ProjectValuesMenuContent(goToProjectValues: (ProjectValueType) -> Unit = {}) {
    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            TaigaHeightSpacer(8.dp)
            ProjectValueType.entries.forEach { type ->
                ListItem(
                    modifier = Modifier.clickable { goToProjectValues(type) },
                    headlineContent = { Text(stringResource(type.toTitleRes())) }
                )
            }
            TaigaHeightSpacer(32.dp)
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun ProjectValuesMenuScreenPreview() {
    TaigaMobilePreviewTheme {
        ProjectValuesMenuContent()
    }
}
