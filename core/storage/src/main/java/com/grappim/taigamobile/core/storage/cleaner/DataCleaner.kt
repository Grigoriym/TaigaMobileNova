package com.grappim.taigamobile.core.storage.cleaner

interface DataCleaner {
    suspend fun cleanOnGoingBackAfterLogin()
}
