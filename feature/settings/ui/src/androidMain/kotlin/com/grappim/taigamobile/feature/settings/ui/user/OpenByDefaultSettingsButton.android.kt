package com.grappim.taigamobile.feature.settings.ui.user

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.open_by_default_setup
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun OpenByDefaultSettingsButton() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        OutlinedButton(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            onClick = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                        Uri.parse("package:${context.packageName}")
                    )
                )
            }
        ) {
            Text(stringResource(RString.open_by_default_setup))
        }
    }
}