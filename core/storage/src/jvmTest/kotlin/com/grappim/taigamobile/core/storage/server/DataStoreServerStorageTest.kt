package com.grappim.taigamobile.core.storage.server

import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.core.storage.createTestDataStore
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `DataStoreServerStorage.server` and `defineServer` are synchronous — they wrap their DataStore
 * access in `runBlocking` — so these tests are deliberately not `runTest`: driving a `runBlocking`
 * getter from inside a virtual-time test scope buys nothing and only obscures what is being called.
 */
class DataStoreServerStorageTest {

    private class FakeAppInfoProvider(
        private val isDebugToReturn: Boolean = false,
        private val debugLocalHostToReturn: String = ""
    ) : AppInfoProvider {
        override fun getAppInfo(): String = "app info"
        override fun isDebug(): Boolean = isDebugToReturn
        override fun isFdroidBuild(): Boolean = false
        override fun getVersionName(): String = "1.0.0"
        override fun getDebugLocalHost(): String = debugLocalHostToReturn
        override fun getBuildType(): String = "debug"
    }

    private fun createSut(appInfoProvider: AppInfoProvider = FakeAppInfoProvider()) = DataStoreServerStorage(
        dataStore = createTestDataStore("server_storage_test"),
        appInfoProvider = appInfoProvider
    )

    @Test
    fun `server falls back to the public Taiga host on a release build`() {
        val sut = createSut(FakeAppInfoProvider(isDebugToReturn = false, debugLocalHostToReturn = LOCAL_HOST))

        assertEquals("https://api.taiga.io", sut.server)
    }

    @Test
    fun `server falls back to the public Taiga host on a debug build with no local host configured`() {
        val sut = createSut(FakeAppInfoProvider(isDebugToReturn = true, debugLocalHostToReturn = ""))

        assertEquals("https://api.taiga.io", sut.server)
    }

    @Test
    fun `server falls back to the configured local host on a debug build`() {
        val sut = createSut(FakeAppInfoProvider(isDebugToReturn = true, debugLocalHostToReturn = LOCAL_HOST))

        assertEquals(LOCAL_HOST, sut.server)
    }

    @Test
    fun `defineServer overrides the default`() {
        val sut = createSut(FakeAppInfoProvider(isDebugToReturn = true, debugLocalHostToReturn = LOCAL_HOST))

        sut.defineServer("https://taiga.example.com")

        assertEquals("https://taiga.example.com", sut.server)
    }

    @Test
    fun `defineServer called twice keeps the latest value`() {
        val sut = createSut()

        sut.defineServer("https://first.example.com")
        sut.defineServer("https://second.example.com")

        assertEquals("https://second.example.com", sut.server)
    }

    @Test
    fun `the storage file name carries the platform preferences extension`() {
        assertEquals("taiga_server_storage_name.preferences_pb", SERVER_STORAGE_FILE_NAME)
    }

    private companion object {
        const val LOCAL_HOST = "http://10.0.2.2:8000"
    }
}
