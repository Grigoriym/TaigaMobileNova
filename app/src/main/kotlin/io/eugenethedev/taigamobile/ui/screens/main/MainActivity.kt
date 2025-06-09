package io.eugenethedev.taigamobile.ui.screens.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import io.eugenethedev.taigamobile.state.ThemeSetting
import io.eugenethedev.taigamobile.ui.theme.TaigaMobileTheme
import io.eugenethedev.taigamobile.ui.theme.taigaMobileRippleTheme
import io.eugenethedev.taigamobile.ui.utils.FilePicker
import io.eugenethedev.taigamobile.ui.utils.LocalFilePicker
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : AppCompatActivity() {

    @SuppressLint("Range")
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it ?: return@registerForActivityResult
        val inputStream = contentResolver.openInputStream(it) ?: return@registerForActivityResult
        val fileName = contentResolver.query(it, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        } ?: return@registerForActivityResult

        filePicker.filePicked(fileName, inputStream)
    }

    private val filePicker: FilePicker = object : FilePicker() {
        override fun requestFile(onFilePicked: (String, InputStream) -> Unit) {
            super.requestFile(onFilePicked)
            getContent.launch("*/*")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: MainViewModel = viewModel()
            val theme by viewModel.theme.collectAsState()

            val darkTheme = when (theme) {
                ThemeSetting.Light -> false
                ThemeSetting.Dark -> true
                ThemeSetting.System -> isSystemInDarkTheme()
            }

            TaigaMobileTheme(darkTheme) {
                CompositionLocalProvider(
                    LocalFilePicker provides filePicker,
                    LocalRippleConfiguration provides taigaMobileRippleTheme()
                ) {
                    MainContent(viewModel)
                }
            }
        }
    }
}
