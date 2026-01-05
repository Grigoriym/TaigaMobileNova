package com.grappim.taigamobile.core.appinfo

import com.grappim.taigamobile.BuildConfig
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import javax.inject.Inject

class AppInfoProviderImpl @Inject constructor() : AppInfoProvider {
    override fun getAppInfo(): String = "${BuildConfig.VERSION_NAME} - " +
        "${BuildConfig.VERSION_CODE} - " +
        BuildConfig.BUILD_TYPE

    override fun isDebug(): Boolean = BuildConfig.DEBUG
    override fun isFdroidBuild(): Boolean = false

    override fun getVersionName(): String = BuildConfig.VERSION_NAME

    override fun getDebugLocalHost(): String = BuildConfig.DEBUG_LOCAL_HOST

    override fun getBuildType(): String = BuildConfig.BUILD_TYPE
}
