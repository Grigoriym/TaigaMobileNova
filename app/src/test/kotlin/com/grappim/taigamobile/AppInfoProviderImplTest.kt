package com.grappim.taigamobile

import com.grappim.taigamobile.core.appinfo.AppInfoProviderImpl
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class AppInfoProviderImplTest {

    private lateinit var sut: AppInfoProvider

    @Before
    fun setup() {
        sut = AppInfoProviderImpl()
    }

    @Test
    fun `on getAppInfo usesBuildConfigValues`() {
        val expectedInfo = "${BuildConfig.VERSION_NAME} - " +
            "${BuildConfig.VERSION_CODE} - " +
            BuildConfig.BUILD_TYPE + " - " + BuildConfig.FLAVOR

        val actualInfo = sut.getAppInfo()

        assertEquals(expectedInfo, actualInfo)
    }

    @Test
    fun `on isDebug returns correct value`() {
        val expectedValue = BuildConfig.DEBUG

        val actualValue = sut.isDebug()

        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `on isFdroidBuild returns correct value`() {
        val expectedValue = BuildConfig.FLAVOR == "fdroid"

        val actualValue = sut.isFdroidBuild()

        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `on getVersionName returns correct value`() {
        val expectedValue = BuildConfig.VERSION_NAME

        val actualValue = sut.getVersionName()

        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `on getDebugLocalHost returns correct value`() {
        val expectedValue = BuildConfig.DEBUG_LOCAL_HOST

        val actualValue = sut.getDebugLocalHost()

        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `on getBuildType returns correct value`() {
        val expectedValue = BuildConfig.BUILD_TYPE

        val actualValue = sut.getBuildType()

        assertEquals(expectedValue, actualValue)
    }
}
