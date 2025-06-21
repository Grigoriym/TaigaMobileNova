package com.grappim.taigamobile.core.storage

sealed class LogoutEvent {
    data object UserInitiated : LogoutEvent()
}
