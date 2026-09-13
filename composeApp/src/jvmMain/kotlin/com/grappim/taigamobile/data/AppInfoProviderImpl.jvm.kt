package com.grappim.taigamobile.data

import com.grappim.kit.appinfo.AppInfoProvider
import com.grappim.taigamobile.BuildKonfig
import com.grappim.taigamobile.core.appinfoapi.DebugLocalHostProvider
import org.koin.core.annotation.Single

@Single(binds = [AppInfoProvider::class, DebugLocalHostProvider::class])
class AppInfoProviderImpl :
    AppInfoProvider,
    DebugLocalHostProvider {
    override fun isDebug(): Boolean = BuildKonfig.IS_DEBUG
    override fun isFdroidBuild(): Boolean = false
    override fun versionName(): String = BuildKonfig.VERSION_NAME
    override fun versionCode(): Int = BuildKonfig.VERSION_CODE.toInt()
    override fun buildType(): String = BuildKonfig.BUILD_TYPE
    override fun getDebugLocalHost(): String = BuildKonfig.DEBUG_LOCAL_HOST
}
