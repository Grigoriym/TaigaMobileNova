package com.grappim.taigamobile.feature.settings.ui.attributes

import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object AttributesScreenNavDestination : NavKey

fun Navigator.goToAttributesScreen() {
    navigate(AttributesScreenNavDestination)
}
