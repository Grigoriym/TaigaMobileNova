package com.grappim.taigamobile.core.appinfoapi

interface AppInfoProvider {
    fun getAppInfo(): String
    fun isDebug(): Boolean
    fun isFdroidBuild(): Boolean
    fun getVersionName(): String
    fun getDebugLocalHost(): String
}
