package com.grappim.taigamobile.core.appinfoapi

interface AppInfoProvider {
    fun getAppInfo(): String
    fun isDebug(): Boolean
    fun isFdroidBuild(): Boolean
}
