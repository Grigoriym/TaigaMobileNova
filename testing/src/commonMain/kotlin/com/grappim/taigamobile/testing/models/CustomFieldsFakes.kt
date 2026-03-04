package com.grappim.taigamobile.testing.models

import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.TextItemState
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString

fun getCustomFieldItemState(): CustomFieldItemState = TextItemState(
    id = getRandomLong(),
    description = getRandomString(),
    label = getRandomString(),
    originalValue = getRandomString(),
    currentValue = getRandomString()
)
