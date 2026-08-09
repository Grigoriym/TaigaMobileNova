package com.grappim.taigamobile.core.storage.network

import com.grappim.taigamobile.core.asynckmp.ApplicationScope
import com.grappim.taigamobile.core.asynckmp.IoDispatcher
import com.grappim.taigamobile.core.logger.LogPriority
import com.grappim.taigamobile.core.logger.logcat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

private const val CHECK_HOST = "1.1.1.1"
private const val CHECK_PORT = 53
private const val CONNECT_TIMEOUT_MS = 2000
private const val CHECK_INTERVAL_MS = 5000L

/**
 * Polls TCP reachability of [CHECK_HOST] on a fixed interval, since the JVM has no OS-level
 * connectivity-change callback API. Starts optimistic ([MutableStateFlow] seeded `true`) and
 * corrects within one [CONNECT_TIMEOUT_MS] of startup — unlike Android's actual, which can query
 * [android.net.ConnectivityManager] synchronously for an immediate accurate initial value.
 */
@Single(binds = [NetworkMonitor::class])
class NetworkMonitorImpl(
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @param:ApplicationScope private val applicationScope: CoroutineScope
) : NetworkMonitor {

    private val _isOnline = MutableStateFlow(true)
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        applicationScope.launch(ioDispatcher) {
            while (isActive) {
                val online = isHostReachable(CHECK_HOST, CHECK_PORT, CONNECT_TIMEOUT_MS)
                if (online != _isOnline.value) {
                    logcat(LogPriority.INFO) { "Network connectivity changed: online=$online" }
                }
                _isOnline.value = online
                delay(CHECK_INTERVAL_MS)
            }
        }
    }
}

internal fun isHostReachable(host: String, port: Int, timeoutMs: Int): Boolean = try {
    Socket().use { socket -> socket.connect(InetSocketAddress(host, port), timeoutMs) }
    true
} catch (e: IOException) {
    false
}
