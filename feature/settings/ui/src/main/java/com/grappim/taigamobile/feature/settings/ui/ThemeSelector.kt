package com.grappim.taigamobile.feature.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.core.storage.ThemeSettings
import com.grappim.taigamobile.feature.settings.ui.interfacescreen.SettingsInterfaceViewState
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.utils.ui.asString

@Composable
fun ThemeSelector(state: SettingsInterfaceViewState, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(RString.theme_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            ThemeSettings.entries.forEachIndexed { index, entry ->
                SegmentedButton(
                    selected = state.themeSettings == entry,
                    onClick = {
                        state.onThemeChanged(entry)
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = ThemeSettings.entries.size
                    )
                ) {
                    Text(state.getThemeTitle(entry).asString(context))
                }
            }
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun ThemeSelectorPreview() {
    TaigaMobileTheme {
        Surface {
            ThemeSelector(
                state = SettingsInterfaceViewState()
            )
        }
    }
}
