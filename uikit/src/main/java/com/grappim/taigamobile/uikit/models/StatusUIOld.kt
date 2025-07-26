package com.grappim.taigamobile.uikit.models

import androidx.compose.ui.graphics.Color
import com.grappim.taigamobile.core.domain.StatusType

@Deprecated("should be removed")
data class StatusUIOld(val id: Long, val name: String, val color: Color, val type: StatusType)
