package com.grappim.taigamobile.core.storage.network

import kotlinx.coroutines.flow.StateFlow

/**
 * Monitors network connectivity state.
 * Used to determine if cached data should be shown when offline.
 */
interface NetworkMonitor {
    /**
     * Emits true when device has active network connection, false otherwise.
     * Initial value reflects current state at subscription time.
     */
    val isOnline: StateFlow<Boolean>
}
