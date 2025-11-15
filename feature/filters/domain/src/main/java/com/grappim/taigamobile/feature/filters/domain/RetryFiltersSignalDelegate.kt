package com.grappim.taigamobile.feature.filters.domain

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface RetryFiltersSignalDelegate {
    val retryFiltersSignal: SharedFlow<Unit>
    suspend fun signalRetryFilters()
}

class RetryFiltersSignalDelegateImpl : RetryFiltersSignalDelegate {

    private val _retryFiltersSignal = MutableSharedFlow<Unit>()
    override val retryFiltersSignal: SharedFlow<Unit> = _retryFiltersSignal.asSharedFlow()

    override suspend fun signalRetryFilters() {
        _retryFiltersSignal.emit(Unit)
    }
}
