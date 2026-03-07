package com.grappim.taigamobile.testing.models

import com.grappim.taigamobile.feature.workitem.ui.models.SelectableTagUI
import com.grappim.taigamobile.testing.utils.getRandomBoolean
import com.grappim.taigamobile.testing.utils.getRandomColor
import com.grappim.taigamobile.testing.utils.getRandomString

fun getTagUI(): SelectableTagUI = SelectableTagUI(
    name = getRandomString(),
    color = getRandomColor(),
    isSelected = getRandomBoolean()
)
