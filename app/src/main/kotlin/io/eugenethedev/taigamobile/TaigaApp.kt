package io.eugenethedev.taigamobile

import android.app.Application
import android.util.Log
import com.google.android.material.color.DynamicColors
import com.grappim.taigamobile.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import io.eugenethedev.taigamobile.utils.FileLoggingTree
import timber.log.Timber

@HiltAndroidApp
class TaigaApp : Application() {

    private var fileLoggingTree: FileLoggingTree? = null
    val currentLogFile get() = fileLoggingTree?.currentFile

    override fun onCreate() {
        super.onCreate()
        val minLoggingPriority = if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Log.DEBUG
        } else {
            Log.WARN
        }

        try {
            fileLoggingTree = FileLoggingTree(
                applicationContext.getExternalFilesDir("logs")!!.absolutePath,
                minLoggingPriority
            )
            Timber.plant(fileLoggingTree!!)
        } catch (e: NullPointerException) {
            Timber.w("Cannot setup FileLoggingTree, skipping")
        }

        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
