package com.grappim.taigamobile.testing.cleaner

import com.grappim.taigamobile.core.storage.cleaner.DataCleaner

class FakeDataCleaner : DataCleaner {

    var cleanOnGoingBackAfterLoginCalled = false

    override suspend fun cleanOnGoingBackAfterLogin() {
        cleanOnGoingBackAfterLoginCalled = true
    }
}
