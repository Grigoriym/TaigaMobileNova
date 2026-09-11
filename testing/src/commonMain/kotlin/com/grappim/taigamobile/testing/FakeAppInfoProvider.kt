package com.grappim.taigamobile.testing

import com.grappim.kit.appinfo.AppInfoProvider

class FakeAppInfoProvider : AppInfoProvider {
    var isDebugToReturn: Boolean = false
    var isFdroidBuildToReturn: Boolean = false
    var versionNameToReturn: String = "1.0.0"
    var versionCodeToReturn: Int = 1
    var buildTypeToReturn: String = "debug"

    override fun isDebug(): Boolean = isDebugToReturn
    override fun isFdroidBuild(): Boolean = isFdroidBuildToReturn
    override fun versionName(): String = versionNameToReturn
    override fun versionCode(): Int = versionCodeToReturn
    override fun buildType(): String = buildTypeToReturn
}
