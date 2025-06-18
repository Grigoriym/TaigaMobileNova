package com.grappim.taigamobile.core.appinfo

interface AppInfoProvider {
    fun getAppInfo(): String
    fun isDebug(): Boolean
    fun isFdroidBuild(): Boolean
}
