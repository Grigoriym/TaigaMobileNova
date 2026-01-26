package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.workitem.ui.models.SelectableTagUI

fun getTagUI(): SelectableTagUI = SelectableTagUI(
    name = getRandomString(),
    color = getRandomColor(),
    isSelected = getRandomBoolean()
)
