package com.grappim.taigamobile.data

import com.grappim.taigamobile.BuildConfig
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import org.koin.core.annotation.Single

@Single(binds = [AppInfoProvider::class])
class AppInfoProviderImpl : AppInfoProvider {
    override fun getAppInfo(): String = "${BuildConfig.VERSION_NAME} - " +
        "${BuildConfig.VERSION_CODE} - " +
        BuildConfig.BUILD_TYPE + " - " + if (BuildConfig.IS_FDROID) "fdroid" else "gplay"

    override fun isDebug(): Boolean = BuildConfig.DEBUG
    override fun isFdroidBuild(): Boolean = BuildConfig.IS_FDROID

    override fun getVersionName(): String = BuildConfig.VERSION_NAME

    override fun getDebugLocalHost(): String = BuildConfig.DEBUG_LOCAL_HOST

    override fun getBuildType(): String = BuildConfig.BUILD_TYPE
}
