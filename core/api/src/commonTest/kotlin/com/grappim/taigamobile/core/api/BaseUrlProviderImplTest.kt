package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.testing.storage.FakeServerStorage
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BaseUrlProviderImplTest {

    private val serverStorage: ServerStorage = FakeServerStorage()

    private lateinit var sut: BaseUrlProvider

    @BeforeTest
    fun setup() {
        sut = BaseUrlProviderImpl(serverStorage = serverStorage)
    }

    @Test
    fun `on getBaseUrl should return correct value`() {
        val actual = sut.getBaseUrl()

        val expected = "${serverStorage.server}/api/v1/"
        assertEquals(expected, actual)
    }
}
