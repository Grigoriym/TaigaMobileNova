package com.grappim.taigamobile.testing.models

import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import com.grappim.taigamobile.testing.utils.getRandomColor
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.StaticColor

fun getStatusUI() = StatusUI(
    id = getRandomLong(),
    title = NativeText.Simple(getRandomString()),
    color = StaticColor(getRandomColor())
)
