package com.grappim.taigamobile.data.api

import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider

private const val DEBUG_TIMEOUT = 5L
private const val PROD_TIMEOUT = 20L

fun getOkHttpTimeout(appInfoProvider: AppInfoProvider): Long = if (appInfoProvider.isDebug()) {
    DEBUG_TIMEOUT
} else {
    PROD_TIMEOUT
}
