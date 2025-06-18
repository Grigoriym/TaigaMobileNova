package com.grappim.taigamobile.main.topbar

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

val LocalTopBarConfig = compositionLocalOf<TopBarController> {
    error("TopBarController not provided")
}

class TopBarController {
    var config by mutableStateOf(TopBarConfig())
        private set

    fun update(config: TopBarConfig) {
        this.config = config
    }

    fun reset() {
        config = TopBarConfig()
    }
}