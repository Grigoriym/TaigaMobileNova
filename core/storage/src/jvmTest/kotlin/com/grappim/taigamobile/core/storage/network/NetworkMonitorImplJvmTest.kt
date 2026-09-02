package com.grappim.taigamobile.core.storage.network

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Exercises [isHostReachable] against real sockets — the JVM actual's underlying platform
 * behaviour, per CLAUDE.md's "prefer the platform whose actual is real" testing convention.
 * 1.1.1.1:53 is a public, highly-available DNS resolver reachable from any runner with outbound
 * network access; 192.0.2.1 is RFC 5737 TEST-NET-1, guaranteed non-routable, so the connect
 * attempt reliably fails rather than depending on a particular local network's failure mode.
 */
class NetworkMonitorImplJvmTest {

    @Test
    fun `isHostReachable returns true for a reachable host`() {
        assertTrue(isHostReachable(host = "1.1.1.1", port = 53, timeoutMs = 2000))
    }

    @Test
    fun `isHostReachable returns false for an unroutable host`() {
        assertFalse(isHostReachable(host = "192.0.2.1", port = 53, timeoutMs = 1000))
    }
}
