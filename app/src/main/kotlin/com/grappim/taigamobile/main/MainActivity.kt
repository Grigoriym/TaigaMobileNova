package com.grappim.taigamobile.main

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.grappim.taigamobile.core.storage.ThemeSettings
import com.grappim.taigamobile.uikit.FilePicker
import com.grappim.taigamobile.uikit.FilePickerOld
import com.grappim.taigamobile.uikit.LocalFilePicker
import com.grappim.taigamobile.uikit.LocalFilePickerOld
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.InputStream

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Deprecated("remove it")
    @SuppressLint("Range")
    private val getContentOld = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it ?: return@registerForActivityResult
        val inputStream = contentResolver.openInputStream(it) ?: return@registerForActivityResult
        val fileName = contentResolver.query(it, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        } ?: return@registerForActivityResult

        filePickerOld.filePicked(fileName, inputStream)
    }

    private val fileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> filePicker.filePicked(uri) }

    private val filePickerOld: FilePickerOld = object : FilePickerOld() {
        override fun requestFile(onFilePicked: (String, InputStream) -> Unit) {
            super.requestFile(onFilePicked)
            getContentOld.launch("*/*")
        }
    }

    private val filePicker: FilePicker = object : FilePicker() {
        override fun requestFile(onFilePicked: (Uri?) -> Unit) {
            super.requestFile(onFilePicked)
            fileLauncher.launch("*/*")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val theme by viewModel.theme.collectAsState()

            val darkTheme = when (theme) {
                ThemeSettings.Light -> false
                ThemeSettings.Dark -> true
                ThemeSettings.System -> isSystemInDarkTheme()
            }

            TaigaMobileTheme(darkTheme) {
                CompositionLocalProvider(
                    LocalFilePickerOld provides filePickerOld,
                    LocalFilePicker provides filePicker
                ) {
                    MainContent(viewModel)
                }
            }
        }
    }
}
