package com.grappim.taigamobile

import com.grappim.taigamobile.core.api.BaseUrlProvider
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.data.BaseUrlProviderImpl
import com.grappim.taigamobile.testing.utils.getRandomString
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test

class BaseUrlProviderImplTest {

    private val serverStorage: ServerStorage = mockk()

    private lateinit var sut: BaseUrlProvider

    @BeforeTest
    fun setup() {
        sut = BaseUrlProviderImpl(serverStorage = serverStorage)
    }

    @Test
    fun `on getBaseUrl should return correct value`() {
        val serverName = getRandomString()
        every { serverStorage.server } returns serverName

        val actual = sut.getBaseUrl()

        val expected = "$serverName/api/v1/"
        assert(actual == expected)
    }
}
