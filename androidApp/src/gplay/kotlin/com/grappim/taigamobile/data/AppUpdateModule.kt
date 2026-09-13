package com.grappim.taigamobile.data

import android.content.Context
import com.grappim.kit.appupdate.AppUpdateChecker
import com.grappim.kit.appupdate.AppUpdateCheckerImpl
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class AppUpdateModule {
    @Single
    fun provideAppUpdateChecker(context: Context): AppUpdateChecker = AppUpdateCheckerImpl(context)
}
