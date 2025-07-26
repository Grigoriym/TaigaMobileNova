package com.grappim.taigamobile.feature.workitem.ui.models

import androidx.compose.ui.graphics.Color
import com.grappim.taigamobile.utils.ui.serializers.ColorSerializer
import kotlinx.serialization.Serializable

@Serializable
data class TagUI(
    val name: String,
    @Serializable(with = ColorSerializer::class)
    val color: Color,
    val isSelected: Boolean = false
)
