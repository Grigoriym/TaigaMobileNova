package com.grappim.taigamobile.testing.storage

import com.grappim.taigamobile.core.storage.customheaders.CustomHeader
import com.grappim.taigamobile.core.storage.customheaders.CustomHeadersStorage
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCustomHeadersStorage(initial: List<CustomHeader> = emptyList()) : CustomHeadersStorage {

    private val _headers = MutableStateFlow(initial)
    override val headers: Flow<List<CustomHeader>> = _headers.asStateFlow()

    override val currentHeaders: List<CustomHeader>
        get() = _headers.value

    var setHeadersThrows = false
    var setHeadersCallCount = 0

    override suspend fun setHeaders(headers: List<CustomHeader>) {
        setHeadersCallCount++
        if (setHeadersThrows) throw testException
        _headers.value = headers
    }
}
