package com.grappim.taigamobile

import android.app.Application
import android.os.StrictMode
import android.util.Log
import com.google.android.material.color.DynamicColors
import com.grappim.taigamobile.utils.FileLoggingTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class TaigaApp : Application() {

    private var fileLoggingTree: FileLoggingTree? = null

    override fun onCreate() {
        super.onCreate()
        setupStrictMode()

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
            Timber.e(e, "Cannot setup FileLoggingTree, skipping")
        }

        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    private fun setupStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
    }
}
