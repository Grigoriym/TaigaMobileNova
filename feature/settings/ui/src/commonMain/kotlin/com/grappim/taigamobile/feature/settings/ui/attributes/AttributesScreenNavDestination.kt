package com.grappim.taigamobile.feature.settings.ui.attributes

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object AttributesScreenNavDestination : NavKey

fun Navigator.goToAttributesScreen() {
    navigate(AttributesScreenNavDestination)
}
