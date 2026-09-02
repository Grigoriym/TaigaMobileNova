package com.grappim.taigamobile.core.navigation

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ResultBusTest {

    @Test
    fun `sendResult before a collector attaches is delivered once one starts collecting`() = runTest {
        val bus = ResultBus()

        bus.sendResult("key", "hello")

        assertEquals("hello", bus.getResultFlow("key").first())
    }

    @Test
    fun `sendResult keys results independently per resultKey`() = runTest {
        val bus = ResultBus()

        bus.sendResult("a", 1)
        bus.sendResult("b", 2)

        assertEquals(1, bus.getResultFlow("a").first())
        assertEquals(2, bus.getResultFlow("b").first())
    }

    @Test
    fun `reified sendResult keys by the result type`() = runTest {
        val bus = ResultBus()

        bus.sendResult(RefreshSignal)

        assertEquals(RefreshSignal, bus.getResultFlow(RefreshSignal::class.toString()).first())
    }

    private data object RefreshSignal
}
