package com.grappim.taigamobile.feature.login.ui

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object LoginNavDestination : NavKey

fun Navigator.navigateToLoginAsTopDestination() {
    resetTo(LoginNavDestination)
}
