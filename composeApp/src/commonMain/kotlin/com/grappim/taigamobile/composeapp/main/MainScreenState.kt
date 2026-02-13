package com.grappim.taigamobile.composeapp.main

data class MainScreenState(
    val isLogoutConfirmationVisible: Boolean = false,
    val setIsLogoutConfirmationVisible: (Boolean) -> Unit = {},
    val onLogout: () -> Unit
)
