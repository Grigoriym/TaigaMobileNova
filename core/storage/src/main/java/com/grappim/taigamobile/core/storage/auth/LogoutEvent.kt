package com.grappim.taigamobile.core.storage.auth

sealed class LogoutEvent {
    data object UserInitiated : LogoutEvent()
}
