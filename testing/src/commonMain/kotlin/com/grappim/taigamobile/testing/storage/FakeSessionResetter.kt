package com.grappim.taigamobile.testing.storage

import com.grappim.taigamobile.core.storage.SessionResetter

class FakeSessionResetter : SessionResetter {
    var resetCalled = false

    override fun reset() {
        resetCalled = true
    }
}
