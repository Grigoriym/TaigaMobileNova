package com.grappim.taigamobile.core.appinfo

import com.grappim.taigamobile.BuildConfig
import javax.inject.Inject

class AppInfoProviderImpl @Inject constructor() :
    com.grappim.taigamobile.core.appinfoapi.AppInfoProvider {
    override fun getAppInfo(): String = "${BuildConfig.VERSION_NAME} - " +
        "${BuildConfig.VERSION_CODE} - " +
        BuildConfig.BUILD_TYPE

    override fun isDebug(): Boolean = BuildConfig.DEBUG
    override fun isFdroidBuild(): Boolean = false
}
