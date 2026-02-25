package com.grappim.taigamobile.core.storage.cache

import com.grappim.taigamobile.core.storage.cleaner.DataCleaner
import com.grappim.taigamobile.core.storage.cleaner.DataCleanerImpl
import com.grappim.taigamobile.testing.storage.FakeAuthStorage
import com.grappim.taigamobile.testing.storage.FakeCacheManager
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class DataCleanerImplTest {

    private val authStorage = FakeAuthStorage()
    private val taigaSessionStorage = FakeTaigaSessionStorage()
    private val cacheManager = FakeCacheManager()

    private lateinit var sut: DataCleaner

    @BeforeTest
    fun setup() {
        sut = DataCleanerImpl(
            authStorage = authStorage,
            taigaSessionStorage = taigaSessionStorage,
            cacheManager = cacheManager
        )
    }

    @Test
    fun `on cleanOnGoingBackAfterLogin should clean needed stuff`() = runTest {
        sut.cleanOnGoingBackAfterLogin()

        assertTrue(authStorage.clearCalled)
        assertTrue(taigaSessionStorage.clearDataCalled)
        assertTrue(cacheManager.clearAllCacheCalled)
    }
}
