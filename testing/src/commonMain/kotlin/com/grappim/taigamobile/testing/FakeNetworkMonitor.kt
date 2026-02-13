package com.grappim.taigamobile.testing

import com.grappim.taigamobile.core.storage.network.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fake implementation of [NetworkMonitor] for testing.
 * Allows tests to control online/offline state.
 */
class FakeNetworkMonitor(
    initialOnline: Boolean = true
) : NetworkMonitor {

    private val _isOnline = MutableStateFlow(initialOnline)
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    fun setOnline(online: Boolean) {
        _isOnline.value = online
    }
}
