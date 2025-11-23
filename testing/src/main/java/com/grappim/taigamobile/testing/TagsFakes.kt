package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.workitem.ui.models.TagUI

fun getTagUI(): TagUI = TagUI(
    name = getRandomString(),
    color = getRandomColor(),
    isSelected = getRandomBoolean()
)
