package com.grappim.taigamobile.data

import com.grappim.kit.appinfo.AppInfoProvider
import com.grappim.taigamobile.BuildConfig
import com.grappim.taigamobile.core.appinfoapi.DebugLocalHostProvider
import org.koin.core.annotation.Single

@Single(binds = [AppInfoProvider::class, DebugLocalHostProvider::class])
class AppInfoProviderImpl : AppInfoProvider, DebugLocalHostProvider {
    override fun isDebug(): Boolean = BuildConfig.DEBUG
    override fun isFdroidBuild(): Boolean = BuildConfig.FLAVOR == "fdroid"

    override fun versionName(): String = BuildConfig.VERSION_NAME

    override fun versionCode(): Int = BuildConfig.VERSION_CODE

    override fun buildType(): String = BuildConfig.BUILD_TYPE

    override fun getDebugLocalHost(): String = BuildConfig.DEBUG_LOCAL_HOST
}
