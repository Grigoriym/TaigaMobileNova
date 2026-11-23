package com.grappim.taigamobile.utils.ui

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class SearchQueryTest {

    @Test
    fun `the initial empty query passes immediately`() = runTest {
        MutableStateFlow("").debounceSearchQuery().test {
            assertEquals("", awaitItem())
        }
    }

    @Test
    fun `quick keystrokes pass only the last query, once typing pauses`() = runTest {
        val query = MutableStateFlow("")

        query.debounceSearchQuery().test {
            assertEquals("", awaitItem())

            query.value = "i"
            advanceTimeBy(100)
            query.value = "is"
            advanceTimeBy(100)
            query.value = "iss"
            advanceTimeBy(SEARCH_QUERY_DEBOUNCE_MILLIS - 1)
            expectNoEvents()

            advanceTimeBy(2)
            assertEquals("iss", awaitItem())
        }
    }

    @Test
    fun `clearing the field passes immediately`() = runTest {
        val query = MutableStateFlow("")

        query.debounceSearchQuery().test {
            assertEquals("", awaitItem())
            query.value = "abc"
            advanceTimeBy(SEARCH_QUERY_DEBOUNCE_MILLIS + 1)
            assertEquals("abc", awaitItem())

            query.value = ""
            advanceTimeBy(1)

            assertEquals("", awaitItem())
        }
    }
}
