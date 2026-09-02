package com.grappim.taigamobile.testing

import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider

class FakeAppInfoProvider : AppInfoProvider {
    var appInfoToReturn: String = "app info"
    var isDebugToReturn: Boolean = false
    var isFdroidBuildToReturn: Boolean = false
    var versionNameToReturn: String = "1.0.0"
    var debugLocalHostToReturn: String = "localhost"
    var buildTypeToReturn: String = "debug"

    override fun getAppInfo(): String = appInfoToReturn
    override fun isDebug(): Boolean = isDebugToReturn
    override fun isFdroidBuild(): Boolean = isFdroidBuildToReturn
    override fun getVersionName(): String = versionNameToReturn
    override fun getDebugLocalHost(): String = debugLocalHostToReturn
    override fun getBuildType(): String = buildTypeToReturn
}
