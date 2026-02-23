package com.grappim.taigamobile.data

import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import org.koin.core.annotation.Single

// todo for sure fix it
@Single(binds = [AppInfoProvider::class])
class AppInfoProviderImpl : AppInfoProvider {
    override fun getAppInfo(): String = "ios"

    override fun isDebug(): Boolean = true
    override fun isFdroidBuild(): Boolean = false

    override fun getVersionName(): String = "dev"

    override fun getDebugLocalHost(): String = ""

    override fun getBuildType(): String = "ios"
}
