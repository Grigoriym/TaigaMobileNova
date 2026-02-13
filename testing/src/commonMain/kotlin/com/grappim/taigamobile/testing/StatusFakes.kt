package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.StaticColor

fun getStatusUI() = StatusUI(
    id = getRandomLong(),
    title = NativeText.Simple(getRandomString()),
    color = StaticColor(getRandomColor())
)
