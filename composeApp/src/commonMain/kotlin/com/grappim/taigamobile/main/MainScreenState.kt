package com.grappim.taigamobile.main

data class MainScreenState(
    val isLogoutConfirmationVisible: Boolean = false,
    val setIsLogoutConfirmationVisible: (Boolean) -> Unit = {},
    val onLogout: () -> Unit
)
