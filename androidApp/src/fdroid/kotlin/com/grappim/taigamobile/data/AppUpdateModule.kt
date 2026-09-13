package com.grappim.taigamobile.data

import com.grappim.kit.appupdate.AppUpdateChecker
import com.grappim.kit.appupdate.AppUpdateCheckerImpl
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class AppUpdateModule {
    @Single
    fun provideAppUpdateChecker(): AppUpdateChecker = AppUpdateCheckerImpl()
}
