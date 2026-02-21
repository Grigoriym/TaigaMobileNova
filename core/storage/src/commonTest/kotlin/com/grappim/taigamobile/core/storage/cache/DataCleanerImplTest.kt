package com.grappim.taigamobile.core.storage.cache

import com.grappim.taigamobile.core.storage.auth.AuthStorage
import com.grappim.taigamobile.core.storage.cleaner.DataCleaner
import com.grappim.taigamobile.core.storage.cleaner.DataCleanerImpl
import kotlinx.coroutines.test.runTest
import org.junit.Before
import kotlin.test.Test

class DataCleanerImplTest {

    private val authStorage: AuthStorage = mockk()
    private val taigaSessionStorage: KmpTaigaSessionStorage = mockk()
    private val cacheManager: CacheManager = mockk()

    private lateinit var sut: DataCleaner

    @Before
    fun setup() {
        sut = DataCleanerImpl(
            authStorage = authStorage,
            taigaSessionStorage = taigaSessionStorage,
            cacheManager = cacheManager
        )
    }

    @Test
    fun `on cleanOnGoingBackAfterLogin should clean needed stuff`() = runTest {
        every { authStorage.clear() } just Runs
        coEvery { taigaSessionStorage.clearData() } just Runs
        coEvery { cacheManager.clearAllCache() } just Runs

        sut.cleanOnGoingBackAfterLogin()

        verify { authStorage.clear() }
        coVerify { taigaSessionStorage.clearData() }
        coVerify { cacheManager.clearAllCache() }
    }
}
