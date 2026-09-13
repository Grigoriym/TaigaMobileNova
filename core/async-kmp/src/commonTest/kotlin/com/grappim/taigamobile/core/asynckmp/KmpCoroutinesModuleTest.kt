@file:OptIn(ExperimentalCoroutinesApi::class)

package com.grappim.taigamobile.core.asynckmp

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.Test
import kotlin.test.assertSame

internal class KmpCoroutinesModuleTest {

    private val sut = KmpCoroutinesModule()

    @Test
    fun `provideApplicationScope delegates to the given dispatcher`() {
        val dispatcher = UnconfinedTestDispatcher()

        val scope = sut.provideApplicationScope(dispatcher)

        assertSame(dispatcher, scope.coroutineContext[ContinuationInterceptor])
    }
}
