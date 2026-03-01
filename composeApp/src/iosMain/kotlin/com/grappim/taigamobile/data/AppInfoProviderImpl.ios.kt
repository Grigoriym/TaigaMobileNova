package com.grappim.taigamobile.data

import com.grappim.taigamobile.BuildKonfig
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import org.koin.core.annotation.Single

@Single(binds = [AppInfoProvider::class])
class AppInfoProviderImpl : AppInfoProvider {
    override fun isDebug(): Boolean = BuildKonfig.IS_DEBUG
    override fun isFdroidBuild(): Boolean = false
    override fun getVersionName(): String = BuildKonfig.VERSION_NAME
    override fun getBuildType(): String = BuildKonfig.BUILD_TYPE
    override fun getDebugLocalHost(): String = BuildKonfig.DEBUG_LOCAL_HOST
    override fun getAppInfo(): String =
        "${BuildKonfig.VERSION_NAME} - ${BuildKonfig.VERSION_CODE} - ${BuildKonfig.BUILD_TYPE}"
}
